package sonar.logistics.api.displays;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.DirtyPart;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.sync.SyncableList;
import sonar.core.utils.CustomColour;
import sonar.logistics.PL2;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.api.displays.buttons.CreateElementButton;
import sonar.logistics.api.displays.elements.ElementSelectionType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.ILookableElement;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.displays.storage.DisplayElementList;
import sonar.logistics.api.displays.storage.EditContainer;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayScreenLook;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.IScaleableDisplay;
import sonar.logistics.api.tiles.signaller.ComparableObject;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.client.gsi.GSIOverlays;
import sonar.logistics.client.gui.display.GuiEditElementsList;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.displays.LocalProviderHandler;

public class DisplayGSI extends DirtyPart implements ISyncPart, ISyncableListener, IFlexibleGui<IDisplay> {

	public final IDisplay display;
	// the watched id, followed by how many references it has been attached to, it is loaded by the IDisplayElements
	// public Map<InfoUUID, Integer> infoReferences = Maps.newHashMap();
	public List<InfoUUID> references = Lists.newArrayList();
	public Map<InfoUUID, IInfo> cachedInfo = Maps.newHashMap();
	public Map<Integer, DisplayElementContainer> containers = Maps.newHashMap();
	public List<Integer> changedElements = Lists.newArrayList();

	public SyncableList syncParts = new SyncableList(this);

	public static final int EDIT_CONTAINER_ID = 0;

	// click info
	public long lastClickTime;
	public UUID lastClickUUID;

	// client side - look element
	public IDisplayElement lookElement = null;
	public double lookX, lookY;
	private long lastLookElementUpdate;

	public SyncTagType.INT container_identity = (INT) new SyncTagType.INT(0).setDefault(-1);
	public SyncTagType.BOOLEAN edit_mode = (BOOLEAN) new SyncTagType.BOOLEAN(2).setDefault(true);

	private double[] currentScaling;

	/// grid selection mode
	public CreateInfoType createInfo;
	public int containerResizing;
	public boolean isGridSelectionMode = false;
	double[] clickPosition1;
	double[] clickPosition2;

	/// element selection mode
	public ElementSelectionType selectionType;
	public boolean isElementSelectionMode = false;
	public List<Integer> selected_identities = Lists.newArrayList();

	{
		syncParts.addParts(container_identity, edit_mode);// , width, height, scale);
	}

	public DisplayGSI(IDisplay display, int id) {
		this.display = display;
		container_identity.setObject(id);
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) // FIXME
			EditContainer.addEditContainer(this);
	}

	//// MAIN ACTIONS \\\\

	public boolean onClicked(TileAbstractDisplay part, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (LogisticsHelper.isPlayerUsingOperator(player)) {
			if (!world.isRemote) {
				edit_mode.invert();
				player.sendMessage(new TextComponentTranslation("Edit Mode: " + edit_mode.getObject()));
				sendInfoContainerPacket();
			}
			return true;
		}

		if (display instanceof ConnectedDisplay) {
			if (!((ConnectedDisplay) display).canBeRendered.getObject()) {
				if (world.isRemote) // left click is client only.
					player.sendMessage(new TextComponentTranslation("THE DISPLAY IS INCOMPLETE"));
				return true;
			}
		}

		if (world.isRemote) { // only clicks on client side, like a GUI, positioning may not be the same on server

			DisplayScreenClick click = InteractionHelper.getClickPosition(this, pos, type, facing, hitX, hitY, hitZ);
			click.setDoubleClick(wasDoubleClick(world, player));
			if (this.isGridSelectionMode) {
				if (type.isShifting()) {
					if (type.isLeft() || clickPosition1 == null) {
						exitGridSelectionMode();
					} else {
						finishGridSelectionMode();
					}
				} else {
					double[] newPosition = new double[] { click.clickX - 0.0625, click.clickY - 0.0625 };
					if (clickPosition1 == null) {
						clickPosition1 = newPosition;

					} else if (clickPosition2 == null) {
						clickPosition2 = newPosition;
					} else if (type.isLeft()) {
						clickPosition1 = newPosition;
					} else {
						clickPosition2 = newPosition;
					}

				}
			} else {
				Tuple<IDisplayElement, double[]> e = getElementFromXY(click.clickX - 0.0625, click.clickY - 0.0625);
				if ((e == null || e.getFirst() == null || !isEditContainer(e.getFirst().getHolder().getContainer())) && isElementSelectionMode) {
					for (DisplayElementContainer container : containers.values()) {
						if (!isEditContainer(container) && container.canRender() && container.canClickContainer(click.clickX - 0.0625, click.clickY - 0.0625)) {
							onElementSelected(container.getContainerIdentity(), type);
							break;
						}
					}
				} else {
					if (e != null && e.getFirst() instanceof IClickableElement) {
						int gui = ((IClickableElement) e.getFirst()).onGSIClicked(click, player, e.getSecond()[0], e.getSecond()[1]);
						if (gui != -1) {
							requestGui(part, world, pos, player, e.getFirst().getElementIdentity(), gui);
						}
					}
				}
			}
		}

		return true; // FIXME

	}

	public Tuple<IDisplayElement, double[]> getElementFromXY(double x, double y) {
		for (DisplayElementContainer container : containers.values()) {
			if (container.canRender() && container.canClickContainer(x, y)) {
				Tuple<IDisplayElement, double[]> e = container.getClickedElement(x, y);
				if (e != null) {
					return e;
				}
			}
		}
		return null;
	}

	private boolean wasDoubleClick(World world, EntityPlayer player) {
		boolean doubleClick = false;
		if (world.getTotalWorldTime() - lastClickTime < 10 && player.getPersistentID().equals(lastClickUUID)) {
			doubleClick = true;
		}
		lastClickTime = world.getTotalWorldTime();
		lastClickUUID = player.getPersistentID();
		return doubleClick;

	}

	public void updateLookElement() {
		DisplayScreenLook look = GSIOverlays.getCurrentLook(this);
		lookElement = null;
		if (look != null) {
			Tuple<IDisplayElement, double[]> e = getElementFromXY(look.lookX - 0.0625, look.lookY - 0.0625);
			if (e != null && e.getFirst() != null && e.getFirst() instanceof ILookableElement) {
				lookElement = e.getFirst();
				lookX = e.getSecond()[0];
				lookY = e.getSecond()[1];
			}
		}
	}

	public void render() {
		if (lastLookElementUpdate == 0 || (System.currentTimeMillis() - lastLookElementUpdate) > 50) {
			lastLookElementUpdate = System.currentTimeMillis();
			updateLookElement();
		}

		// renders viewable the display element containers

		if (isGridSelectionMode) {
			GlStateManager.translate(0, 0, -0.01);
			GlStateManager.pushMatrix();

			// render the other containers
			for (DisplayElementContainer container : containers.values()) {
				if (container.canRender() && !isEditContainer(container)) {
					double[] translation = container.getTranslation();
					double[] scaling = container.getContainerMaxScaling();
					DisplayElementHelper.drawRect(translation[0], translation[1], translation[0] + scaling[0], translation[1] + scaling[1], new CustomColour(255, 153, 51).getRGB());
				}
			}

			/// renders the click selections
			if (clickPosition1 != null) {
				GlStateManager.translate(0, 0, -0.01);
				double[] click2 = clickPosition2 == null ? clickPosition1 : clickPosition2;
				double clickStartX = GSIHelper.getGridXPosition(this, Math.min(clickPosition1[0], click2[0]));
				double clickStartY = GSIHelper.getGridYPosition(this, Math.min(clickPosition1[1], click2[1]));
				double clickEndX = Math.min(getDisplayScaling()[0], GSIHelper.getGridXPosition(this, Math.max(clickPosition1[0], click2[0])) + GSIHelper.getGridXScale(this));
				double clickEndY = Math.min(getDisplayScaling()[1], GSIHelper.getGridYPosition(this, Math.max(clickPosition1[1], click2[1])) + GSIHelper.getGridYScale(this));
				DisplayElementHelper.drawRect(clickStartX, clickStartY, clickEndX, clickEndY, new CustomColour(49, 145, 88).getRGB());
			}

			/// render the grid
			GlStateManager.translate(0, 0, -0.01);
			CustomColour green = new CustomColour(174, 227, 227);
			DisplayElementHelper.drawGrid(0, 0, getDisplayScaling()[0], getDisplayScaling()[1], GSIHelper.getGridXScale(this), GSIHelper.getGridYScale(this), green.getRGB());

			/// render help overlays
			/* GlStateManager.translate(0, 0, -0.001); List<String> messages = Lists.newArrayList(); if (clickPosition1 == null && clickPosition2 == null) { messages.add("L-CLICK = SELECT START POSITION"); } else if (clickPosition1 != null && clickPosition2 == null) { messages.add("R-CLICK = SELECT END POSITION"); } else if (clickPosition1 != null && clickPosition2 != null) { messages.add("SHIFT-R = CONFIRM"); } messages.add("SHIFT-L = CANCEL"); InfoRenderer.renderCenteredStringsWithUniformScaling(messages, getDisplayScaling()[0], getDisplayScaling()[1], 0, 0.75, green.getRGB()); */
			GlStateManager.popMatrix();
		} else {
			getViewableContainers().forEach(DisplayElementContainer::render);
		}

	}

	//// ELEMENT SELECTION MODE \\\\

	public void startElementSelectionMode(ElementSelectionType type) {
		selectionType = type;
		selected_identities = Lists.newArrayList();
		isElementSelectionMode = true;
	}

	public void onElementSelected(int containerID, BlockInteractionType type) {
		if (type == BlockInteractionType.RIGHT) {
			if (selected_identities.contains(containerID)) {
				selected_identities.remove(Integer.valueOf(containerID));
			} else {
				selected_identities.add(containerID);
			}
		}
		if (type == BlockInteractionType.SHIFT_LEFT) {
			finishElementSelectionMode(false);
		}

		if (type == BlockInteractionType.SHIFT_RIGHT) {
			if (!selected_identities.isEmpty()) {
				finishElementSelectionMode(true);
			}
		}
	}

	public void finishElementSelectionMode(boolean sendPacket) {
		if (sendPacket)
			selectionType.finishSelection(this, selected_identities);
		selectionType = null;
		selected_identities = Lists.newArrayList();
		isElementSelectionMode = false;
	}

	public void startResizeSelectionMode(int containerID) {
		DisplayElementContainer c = getContainer(containerID);
		if (c != null) {
			createInfo = null;
			isGridSelectionMode = true;
			containerResizing = c.getContainerIdentity();
			clickPosition1 = c.getTranslation();
			clickPosition2 = new double[] { c.getTranslation()[0] + c.getContainerMaxScaling()[0], c.getTranslation()[1] + c.getContainerMaxScaling()[1], 0 };

			Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("" + //
					TextFormatting.GREEN + "L-CLICK" + TextFormatting.RESET + " = FIRST POSITION, " + //
					TextFormatting.GREEN + "R-CLICK" + TextFormatting.RESET + " = SECOND POSITION, " + //
					TextFormatting.GREEN + "SHIFT-R" + TextFormatting.RESET + " = CONFIRM, " + TextFormatting.RED + "SHIFT-L" + TextFormatting.RESET + " = CANCEL"));
		}
	}

	public void startGridSelectionMode(CreateInfoType type) {
		createInfo = type;
		isGridSelectionMode = true;
		clickPosition1 = null;
		clickPosition2 = null;

		Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("" + //
				TextFormatting.GREEN + "L-CLICK" + TextFormatting.RESET + " = FIRST POSITION, " + //
				TextFormatting.GREEN + "R-CLICK" + TextFormatting.RESET + " = SECOND POSITION, " + //
				TextFormatting.GREEN + "SHIFT-R" + TextFormatting.RESET + " = CONFIRM, " + TextFormatting.RED + "SHIFT-L" + TextFormatting.RESET + " = CANCEL"));
	}

	public void exitGridSelectionMode() {
		isElementSelectionMode = false;
		isGridSelectionMode = false;
		clickPosition1 = null;
		clickPosition2 = null;
	}

	public void finishGridSelectionMode() {
		double[] click2 = clickPosition2 == null ? clickPosition1 : clickPosition2;
		double clickStartX = GSIHelper.getGridXPosition(this, Math.min(clickPosition1[0], click2[0]));
		double clickStartY = GSIHelper.getGridYPosition(this, Math.min(clickPosition1[1], click2[1]));
		double clickEndX = Math.min(getDisplayScaling()[0], GSIHelper.getGridXPosition(this, Math.max(clickPosition1[0], click2[0])) + GSIHelper.getGridXScale(this));
		double clickEndY = Math.min(getDisplayScaling()[1], GSIHelper.getGridYPosition(this, Math.max(clickPosition1[1], click2[1])) + GSIHelper.getGridYScale(this));
		if (createInfo != null) {
			GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createInfoAdditionPacket(new double[] { clickStartX, clickStartY, 0 }, new double[] { clickEndX - clickStartX, clickEndY - clickStartY, 1 }, 0.5, createInfo), -1, this);
		} else {
			GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createResizeContainerPacket(this.containerResizing, new double[] { clickStartX, clickStartY, 0 }, new double[] { clickEndX - clickStartX, clickEndY - clickStartY, 1 }, 0.5), -1, this);
			containerResizing = -1;
		}
		exitGridSelectionMode();
	}

	//// GSI/ELEMENT SCALING \\\\

	public void updateScaling() {
		updateDisplayScaling();
		containers.values().forEach(DisplayElementContainer::updateActualScaling);
	}

	public Stream<DisplayElementContainer> getViewableContainers() {
		return containers.values().stream().filter(DisplayElementContainer::canRender);
	}

	public void updateDisplayScaling() {
		if (display instanceof IScaleableDisplay) {
			currentScaling = ((IScaleableDisplay) display).getScaling();
		} else {
			currentScaling = new double[] { display.getDisplayType().width, display.getDisplayType().height, 1 };
		}
	}

	public double[] getDisplayScaling() {
		if (currentScaling == null) {
			updateDisplayScaling();
		}
		return currentScaling;
	}

	//// INFO REFERENCES \\\\

	public void forEachValidUUID(Consumer<InfoUUID> action) {
		references.forEach(uuid -> action.accept(uuid));
	}

	public boolean isDisplayingUUID(InfoUUID id) {
		return references.contains(id);
	}

	public void onMonitoredListChanged(InfoUUID uuid, AbstractChangeableList list) {
		/// FIXME DON'T DO THIS FOR EVERYTHING
		updateCachedInfo();
		updateScaling();
	}

	public void onInfoChanged(InfoUUID uuid, IInfo info) {
		updateCachedInfo();
		for (DisplayElementContainer c : containers.values()) {
			for (IDisplayElement e : c.getElements()) {
				if (e.getInfoReferences().contains(uuid)) {
					e.onInfoReferenceChanged(uuid, info);
					break;
				}
			}
		}
	}

	//// INFO REFERENCES \\\\

	public void updateInfoReferences() {
		List<InfoUUID> newReferences = Lists.newArrayList();
		containers.values().forEach(c -> c.getElements().forEach(e -> ListHelper.addWithCheck(newReferences, e.getInfoReferences())));
		references = newReferences;
	}

	/** takes the given uuids and adds them as Info References it will also add the host display to the listeners of the Reader which provides the info uuid */
	public void addInfoReferences(List<InfoUUID> uuid) {
		uuid.forEach(this::addInfoReference);
		updateCachedInfo();
	}

	private void addInfoReference(InfoUUID uuid) {
		if (ListHelper.addWithCheck(references, uuid)) {
			LocalProviderHandler.doInfoReferenceConnect(this, uuid);
		}
	}

	/** takes the given uuids and removes them as Info References it will also remove the host display from the listeners of the Reader which provides the info uuid */
	public void removeInfoReferences(List<InfoUUID> uuid) {
		uuid.forEach(this::removeInfoReference);
		updateCachedInfo();
	}

	private void removeInfoReference(InfoUUID uuid) {
		if (references.remove(uuid)) {
			LocalProviderHandler.doInfoReferenceConnect(this, uuid);
		}
	}

	//// CACHED INFO \\\\

	public void updateCachedInfo() {
		IInfoManager manager = PL2.getInfoManager(FMLCommonHandler.instance().getEffectiveSide().isClient());
		Map<InfoUUID, IInfo> newCache = Maps.newHashMap();
		references.forEach(ref -> newCache.put(ref, manager.getInfoFromUUID(ref)));
		cachedInfo = newCache;
	}

	public IInfo getCachedInfo(InfoUUID uuid) {
		return cachedInfo.get(uuid);
	}

	//// GUIS \\\\

	public void requestGui(TileAbstractDisplay display, World world, BlockPos pos, EntityPlayer player, int elementIdentity, int guiID) {
		GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createGuiRequestPacket(guiID), elementIdentity, this);
	}

	public IDisplayElement getElementFromIdentity(int identity) {
		for (DisplayElementContainer c : containers.values()) {
			IDisplayElement e = c.getElements().getElementFromIdentity(identity);
			if (e != null) {
				return e;
			}
		}

		return null;
	}

	public IFlexibleGui getElementFromGuiPacket(IDisplay obj, int containerID, int elementID, World world, EntityPlayer player, NBTTagCompound tag) {
		DisplayElementContainer c = getContainer(containerID);
		if (c != null) {
			IDisplayElement e = c.getElements().getElementFromIdentity(elementID);
			if (e instanceof IFlexibleGui) {
				return (IFlexibleGui) e;
			}
		}
		return null;
	}

	public void onGuiOpened(IDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		int containerID = tag.getInteger("CONT_ID");
		int elementID = tag.getInteger("ELE_ID");
		if (containerID == -1 || elementID == -1) {
			switch (id) {
			case 0:
				TileAbstractDisplay display = (TileAbstractDisplay) obj.getActualDisplay();
				PacketHelper.sendLocalProvidersFromScreen(display, world, display.getPos(), player);
				break;
			}
		} else {
			IFlexibleGui guiHandler = getElementFromGuiPacket(obj, containerID, elementID, world, player, tag);
			if (guiHandler != null) {
				guiHandler.onGuiOpened(obj, id, world, player, tag);
			}
		}
	}

	public Object getServerElement(IDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		int containerID = tag.getInteger("CONT_ID");
		int elementID = tag.getInteger("ELE_ID");
		if (containerID == -1 || elementID == -1) {
			switch (id) {
			case 0:
				TileAbstractDisplay display = (TileAbstractDisplay) obj.getActualDisplay();
				return new ContainerMultipartSync(display);
			}
		} else {
			IFlexibleGui guiHandler = getElementFromGuiPacket(obj, containerID, elementID, world, player, tag);
			if (guiHandler != null) {
				return guiHandler.getServerElement(obj, id, world, player, tag);
			}
		}
		return null;
	}

	public Object getClientElement(IDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		int containerID = tag.getInteger("CONT_ID");
		int elementID = tag.getInteger("ELE_ID");
		if (containerID == -1 || elementID == -1) {
			switch (id) {
			case 0:
				TileAbstractDisplay display = (TileAbstractDisplay) obj.getActualDisplay();
				return new GuiEditElementsList(this, display);
			}
		} else {
			IFlexibleGui guiHandler = getElementFromGuiPacket(obj, containerID, elementID, world, player, tag);
			if (guiHandler != null) {
				return guiHandler.getClientElement(obj, id, world, player, tag);
			}
		}
		return null;
	}

	//// ELEMENTS \\\\

	public DisplayElementContainer getEditContainer() {
		return containers.get(EDIT_CONTAINER_ID);
	}

	public boolean isEditContainer(DisplayElementContainer c) {
		return c.getContainerIdentity() == EDIT_CONTAINER_ID;
	}

	/** creates a new Element Container with the given properties */
	public DisplayElementContainer addElementContainer(double[] translate, double[] scale, double pScale) {
		int identity = createDisplayContainerIdentity();
		DisplayElementContainer container = new DisplayElementContainer(this, translate, scale, pScale, identity);
		containers.put(identity, container);
		sendInfoContainerPacket();
		return container;
	}

	/** removes the Element Container with the given id */
	public void removeElementContainer(int containerID) {
		containers.remove(containerID);
		sendInfoContainerPacket();
		// reset info references???
	}

	public DisplayElementContainer getContainer(int identity) {
		return containers.get(identity);
	}

	public void addElement(int containerID, IDisplayElement element) {
		containers.get(containerID).getElements().addElement(element);
	}

	public void removeElement(int containerID, IDisplayElement element) {
		containers.get(containerID).getElements().removeElement(element);
	}

	public void removeElement(int identity) {
		IDisplayElement element = getElementFromIdentity(identity);
		if (element != null) {
			IElementStorageHolder holder = element.getHolder();
			holder.getElements().removeElement(element);
			if (holder.getElements().getElementCount() == 0) {
				if (holder instanceof DisplayElementContainer) {
					containers.remove(holder);
				} else if (holder instanceof IDisplayElement) {
					removeElement(((IDisplayElement) holder).getElementIdentity());
				}
			}
		}
	}

	public void onElementAdded(IElementStorageHolder c, IDisplayElement e) {
		addInfoReferences(e.getInfoReferences());
		sendInfoContainerPacket();
	}

	public void onElementRemoved(IElementStorageHolder c, IDisplayElement e) {
		removeInfoReferences(e.getInfoReferences());
		sendInfoContainerPacket();
	}

	public void sendInfoContainerPacket() {
		if (display.getCoords() != null && !display.getCoords().getWorld().isRemote) {
			display.sendInfoContainerPacket();
		}
	}

	/* public NBTTagCompound saveElement(IDisplayElement element, SyncType type) { NBTTagCompound elementTag = new NBTTagCompound(); if (type.isGivenType(SyncType.SAVE) || (type.isGivenType(SyncType.DEFAULT_SYNC) && hasElementChanged(element.getElementIdentity()))) { elementTag.setInteger("identity", element.getElementIdentity()); DisplayElementHelper.saveElement(elementTag, element, type); } return elementTag; } public IDisplayElement loadOrUpdateElement(NBTTagCompound tag, SyncType type) { int identity = tag.getInteger("identity"); IDisplayElement element = elements.get(identity); if(element==null){ element = DisplayElementHelper.loadElement(tag); addElement(element); return element; } return null; } */

	public boolean hasElementChanged(int identityID) {
		return changedElements.contains(identityID);
	}

	public void markElementChanged(int identityID) {
		ListHelper.addWithCheck(changedElements, identityID);
	}

	private int createDisplayContainerIdentity() {
		return PL2.getServerManager().getNextIdentity();
	}

	public int getDisplayGSIIdentity() {
		return container_identity.getObject();
	}

	public IDisplay getDisplay() {
		return display;
	}

	//// NBT \\\\

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.SAVE)) {
			List<Integer> loaded = Lists.newArrayList();
			NBTTagList tagList = nbt.getTagList("containers", NBT.TAG_COMPOUND);
			tagList.forEach(tag -> loaded.add(loadContainer((NBTTagCompound) tag, type).getContainerIdentity()));
			// if (type.isType(SyncType.SAVE)) {
			loaded.add(EDIT_CONTAINER_ID);
			List<Integer> toDelete = Lists.newArrayList();
			containers.values().forEach(c -> {
				if (!loaded.contains(c.getContainerIdentity())) {
					toDelete.add(c.getContainerIdentity());
				}
			});
			toDelete.forEach(del -> containers.remove(del));
			// }
		}
		NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
		if (!tag.hasNoTags()) {
			NBTHelper.readSyncParts(tag, type, syncParts);
		}
		updateInfoReferences();
		updateCachedInfo();
		updateScaling();
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.SAVE)) {
			NBTTagList tagList = new NBTTagList();
			containers.values().forEach(c -> {
				NBTTagCompound tag = saveContainer(c, type);
				if (!tag.hasNoTags()) {
					tagList.appendTag(tag);
				}
			});
			nbt.setTag("containers", tagList);
		}
		NBTTagCompound tag = NBTHelper.writeSyncParts(new NBTTagCompound(), type, syncParts, type.mustSync());
		if (!tag.hasNoTags()) {
			nbt.setTag(this.getTagName(), tag);
		}
		return nbt;
	}

	public NBTTagCompound saveContainer(DisplayElementContainer c, SyncType type) {
		if (isEditContainer(c) && !edit_mode.getObject()) {
			return new NBTTagCompound(); // don't send the edit container if it isn't being viewed
		}
		return c.writeData(new NBTTagCompound(), type);
	}

	public DisplayElementContainer loadContainer(NBTTagCompound nbt, SyncType type) {
		int identity = nbt.getInteger("iden");
		DisplayElementContainer container = containers.get(identity);
		if (container == null) {
			container = new DisplayElementContainer();
			container.gsi = this;
			container.readData(nbt, type);
			containers.put(identity, container);
		} else {
			container.readData(nbt, type);
		}
		return container;
	}

	@Override
	public boolean canSync(SyncType sync) {
		return sync.isType(SyncType.SAVE, SyncType.SPECIAL);
	}

	@Override
	public String getTagName() {
		return "gsi";
	}

	@Override
	public void markChanged(IDirtyPart part) {
		markChanged(); // alert the display
	}

	//// ORIENTATION \\\\

	public EnumFacing getFacing() {
		return display.getCableFace();
	}

	public EnumFacing getRotation() {
		return EnumFacing.NORTH; // FIXME - when it's placed set the rotation;
	}
}
