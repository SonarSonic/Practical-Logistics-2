package sonar.logistics.api.displays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.listener.ISonarListener;
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
import sonar.logistics.api.displays.buttons.EmptyInfoElement;
import sonar.logistics.api.displays.elements.ElementSelectionType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.ILookableElement;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.displays.storage.EditContainer;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayScreenLook;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.IScaleableDisplay;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.client.gsi.GSIOverlays;
import sonar.logistics.client.gui.display.GuiEditElementsList;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.displays.ChunkViewerHandler;
import sonar.logistics.networking.displays.DisplayHandler;
import sonar.logistics.networking.displays.LocalProviderHandler;
import sonar.logistics.packets.PacketDisplayGSIContentsPacket;
import sonar.logistics.packets.PacketDisplayGSIValidate;

public class DisplayGSI extends DirtyPart implements ISyncPart, ISyncableListener, IFlexibleGui<IDisplay>, ISonarListener {

	public final IDisplay display;
	public List<InfoUUID> references = new ArrayList<>();
	public Map<InfoUUID, IInfo> cachedInfo = new HashMap<>();
	public Map<Integer, DisplayElementContainer> containers = new HashMap<>();

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

	public double[] currentScaling;

	public boolean isElementSelectionMode = false;
	public boolean isGridSelectionMode = false;
	public GSIGridMode grid_mode = new GSIGridMode(this);
	public GSISelectionMode selection_mode = new GSISelectionMode(this);

	{
		syncParts.addParts(container_identity, edit_mode);// , width, height, scale);
	}

	public final World world;

	public DisplayGSI(IDisplay display, World world, int id) {
		this.display = display;
		this.world = world;
		container_identity.setObject(id);
		if (world != null && world.isRemote)
			EditContainer.addEditContainer(this);
	}

	//// MAIN ACTIONS \\\\

	public boolean onClicked(TileAbstractDisplay part, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (LogisticsHelper.isPlayerUsingOperator(player)) {
			//// SWITCH TO EDIT MODE \\\\
			if (!world.isRemote) {
				edit_mode.invert();
				player.sendMessage(new TextComponentTranslation("Edit Mode: " + edit_mode.getObject()));
				sendInfoContainerPacket();
			}
			return true;
		}

		if (display instanceof ConnectedDisplay && !((ConnectedDisplay) display).canBeRendered.getObject()) {
			if (world.isRemote) { // left click is client only.
				player.sendMessage(new TextComponentTranslation("THE DISPLAY IS INCOMPLETE"));
			}
			return true;
		}

		if (world.isRemote) { // only clicks on client side, like a GUI, positioning may not be the same on server

			DisplayScreenClick click = InteractionHelper.getClickPosition(this, pos, type, facing, hitX, hitY, hitZ);
			click.setDoubleClick(wasDoubleClick(world, player));
			if (this.isGridSelectionMode) {
				grid_mode.onClicked(type, click);
			} else {
				Tuple<IDisplayElement, double[]> clickedElement = getElementFromXY(click.clickX - 0.0625, click.clickY - 0.0625);
				if ((clickedElement == null || clickedElement.getFirst() == null || !isEditContainer(clickedElement.getFirst().getHolder().getContainer())) && isElementSelectionMode) {
					//// COMPLETES ELEMENT SELECTION MODE \\\\
					for (DisplayElementContainer container : containers.values()) {
						if (!isEditContainer(container) && container.canRender() && container.canClickContainer(click.clickX - 0.0625, click.clickY - 0.0625)) {
							selection_mode.onElementSelected(container.getContainerIdentity(), type);
							break;
						}
					}
				} else {
					if (clickedElement != null && this.edit_mode.getObject()) {
						//// NO-SHIFT: OPENS GUI EDIT SCREEN, SHIFT: STARTS RESIZE MODE FOR THE CLICKED ELEMENT \\\\
						if (!isEditContainer(clickedElement.getFirst().getHolder().getContainer()) && !(clickedElement.getFirst() instanceof EmptyInfoElement)) {
							if (!player.isSneaking()) {
								NBTTagCompound guiTag = new NBTTagCompound();
								guiTag.setInteger("clicked", clickedElement.getFirst().getElementIdentity());
								requestGui(part, world, pos, player, -1, 1, guiTag);
							} else {
								grid_mode.startResizeSelectionMode(clickedElement.getFirst().getHolder().getContainer().getContainerIdentity());
							}
							return true;
						}
					}
					//// PERFORM ELEMENT GSI CLICK \\\\
					if (clickedElement != null && clickedElement.getFirst() instanceof IClickableElement) {
						int gui = ((IClickableElement) clickedElement.getFirst()).onGSIClicked(click, player, clickedElement.getSecond()[0], clickedElement.getSecond()[1]);
						if (gui != -1) {
							requestGui(part, world, pos, player, clickedElement.getFirst().getElementIdentity(), gui, new NBTTagCompound());
						}
					}
				}
			}
		}

		return true; // FIXME

	}

	/** gets the Element at the given XY, used to know element clicked/hovered */
	public Tuple<IDisplayElement, double[]> getElementFromXY(double x, double y) {
		for (DisplayElementContainer container : containers.values()) {
			if (container.canRender() && container.canClickContainer(x, y)) {
				Tuple<IDisplayElement, double[]> e = container.getElementFromXY(x, y);
				if (e != null) {
					return e;
				}
			}
		}
		return null;
	}

	/** if the screen was double clicked, called during the click method, don't call it elsewhere */
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
			grid_mode.renderSelectionMode();
		} else {
			getViewableContainers().forEach(DisplayElementContainer::render);
		}

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

	public void forEachElement(Consumer<IDisplayElement> action) {
		forEachContainer(c -> c.getElements().forEach(action));
	}

	public void forEachContainer(Consumer<DisplayElementContainer> action) {
		containers.values().forEach(action);
	}

	public boolean isDisplayingUUID(InfoUUID id) {
		return references.contains(id);
	}

	public void onMonitoredListChanged(InfoUUID uuid, AbstractChangeableList list) {
		updateCachedInfo();
		updateScaling();
		forEachElement(e -> {
			if (e.getInfoReferences().contains(uuid)) {
				e.onChangeableListChanged(uuid, list);				
			}
		});
	}

	public void onInfoChanged(InfoUUID uuid, IInfo info) {
		updateCachedInfo();
		forEachElement(e -> {
			if (e.getInfoReferences().contains(uuid)) {
				e.onInfoReferenceChanged(uuid, info);
			}
		});

	}

	//// INFO REFERENCES \\\\

	public void updateInfoReferences() {
		if (!isValid()) {
			return;
		}
		List<InfoUUID> newReferences = new ArrayList<>();
		forEachContainer(c -> c.getElements().forEach(e -> ListHelper.addWithCheck(newReferences, e.getInfoReferences())));
		List<InfoUUID> removed = new ArrayList<>();
		for (InfoUUID ref : references) {
			if (!newReferences.contains(ref)) {
				removed.add(ref);
				continue;
			}
			newReferences.remove(ref);
		}
		if (!newReferences.isEmpty() || !removed.isEmpty()) {
			newReferences.forEach(n -> LocalProviderHandler.doInfoReferenceConnect(this, n));
			references.addAll(newReferences);
			removed.forEach(r -> LocalProviderHandler.doInfoReferenceDisconnect(this, r));
		}
	}

	//// CACHED INFO \\\\

	public void updateCachedInfo() {
		IInfoManager manager = PL2.proxy.getInfoManager(world.isRemote);
		Map<InfoUUID, IInfo> newCache = new HashMap<>();
		references.forEach(ref -> newCache.put(ref, manager.getInfoFromUUID(ref)));
		cachedInfo = newCache;
	}

	public IInfo getCachedInfo(InfoUUID uuid) {
		return cachedInfo.get(uuid);
	}

	//// GUIS \\\\

	public void requestGui(TileAbstractDisplay display, World world, BlockPos pos, EntityPlayer player, int elementIdentity, int guiID, NBTTagCompound guiTag) {
		GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createGuiRequestPacket(guiID, guiTag), elementIdentity, this);
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
		int containerID = tag.hasKey("CONT_ID") ? tag.getInteger("CONT_ID") : -1;
		int elementID = tag.hasKey("ELE_ID") ? tag.getInteger("ELE_ID") : -1;
		if (containerID == -1 || elementID == -1) {
			switch (id) {
			case 0:
			case 1:
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
		int containerID = tag.hasKey("CONT_ID") ? tag.getInteger("CONT_ID") : -1;
		int elementID = tag.hasKey("ELE_ID") ? tag.getInteger("ELE_ID") : -1;
		if (containerID == -1 || elementID == -1) {
			switch (id) {
			case 0:
			case 1:
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
		int containerID = tag.hasKey("CONT_ID") ? tag.getInteger("CONT_ID") : -1;
		int elementID = tag.hasKey("ELE_ID") ? tag.getInteger("ELE_ID") : -1;
		if (containerID == -1 || elementID == -1) {
			switch (id) {
			case 0:
				TileAbstractDisplay display = (TileAbstractDisplay) obj.getActualDisplay();
				return new GuiEditElementsList(this, display);
			case 1:
				display = (TileAbstractDisplay) obj.getActualDisplay();
				int element_id = tag.getInteger("clicked");
				IDisplayElement element = getElementFromIdentity(element_id);
				return element.getClientEditGui(display, null, world, player);
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
					containers.remove(((DisplayElementContainer) holder).getContainerIdentity());
				} else if (holder instanceof IDisplayElement) {
					removeElement(((IDisplayElement) holder).getElementIdentity());
				}
			}
		}
	}

	public void onElementAdded(IElementStorageHolder c, IDisplayElement e) {
		updateInfoReferences();
		sendInfoContainerPacket();
	}

	public void onElementRemoved(IElementStorageHolder c, IDisplayElement e) {
		updateInfoReferences();
		sendInfoContainerPacket();
	}

	public void sendInfoContainerPacket() {
		if (world != null && display != null && !world.isRemote) {
			if (!isValid()) {
				return;
			}
			List<EntityPlayerMP> players = ChunkViewerHandler.instance().getWatchingPlayers(this);
			players.forEach(listener -> PL2.network.sendTo(new PacketDisplayGSIContentsPacket(this), listener));
			this.display.onInfoContainerPacket();
		}
	}

	private int createDisplayContainerIdentity() {
		return ServerInfoHandler.instance().getNextIdentity();
	}

	public int getDisplayGSIIdentity() {
		return container_identity.getObject();
	}

	public IDisplay getDisplay() {
		return display;
	}

	public World getWorld() {
		return world;
	}

	//// NBT \\\\

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.SAVE)) {
			List<Integer> loaded = new ArrayList<>();
			NBTTagList tagList = nbt.getTagList("containers", NBT.TAG_COMPOUND);
			tagList.forEach(tag -> loaded.add(loadContainer((NBTTagCompound) tag, type).getContainerIdentity()));
			loaded.add(EDIT_CONTAINER_ID);
			List<Integer> toDelete = new ArrayList<>();
			forEachContainer(c -> {
				if (!loaded.contains(c.getContainerIdentity())) {
					toDelete.add(c.getContainerIdentity());
				}
			});
			toDelete.forEach(del -> containers.remove(del));
		}
		NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
		if (!tag.hasNoTags()) {
			NBTHelper.readSyncParts(tag, type, syncParts);
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.SAVE)) {
			NBTTagList tagList = new NBTTagList();
			forEachContainer(c -> {
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

	//// VALIDATION \\\\

	public boolean isValid = false;

	@Override
	public boolean isValid() {
		return isValid;
	}

	public void validate() {
		if (!isValid) {
			isValid = true;
			updateInfoReferences();
			updateCachedInfo();
			updateScaling();
			display.onGSIValidate();
			if (!world.isRemote) {
				if (display instanceof ConnectedDisplay) {
					DisplayHandler.updateWatchers(Lists.newArrayList(), (ConnectedDisplay) display);
				}
				PL2.proxy.getServerManager().displays.put(getDisplayGSIIdentity(), this);
				List<EntityPlayerMP> watchers = ChunkViewerHandler.instance().getWatchingPlayers(this);
				watchers.forEach(watcher -> PL2.network.sendTo(new PacketDisplayGSIValidate(this, display), watcher));
			} else {
				PL2.proxy.getClientManager().displays_gsi.put(getDisplayGSIIdentity(), this);
			}
		}
	}

	public void invalidate() {
		if (isValid) {
			isValid = false;
			display.onGSIInvalidate();
			if (!world.isRemote) {
				PL2.proxy.getServerManager().displays.remove(getDisplayGSIIdentity());
			} else {
				PL2.proxy.getClientManager().displays_gsi.remove(getDisplayGSIIdentity());
			}
		}
	}
}
