package sonar.logistics.api.displays;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.sync.DirtyPart;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncableList;
import sonar.core.utils.CustomColour;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncTagType.DOUBLE;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.api.displays.elements.ButtonElement;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.DisplayElementList;
import sonar.logistics.api.displays.elements.HeightAlignment;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.ILookableElement;
import sonar.logistics.api.displays.elements.WidthAlignment;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayScreenLook;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.IScaleableDisplay;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gsi.GSIButton;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.client.gsi.GSIOverlays;
import sonar.logistics.client.gsi.GSIPackets;
import sonar.logistics.client.gsi.IGSI;
import sonar.logistics.client.gsi.IGSIListViewer;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.packets.PacketGSIClick;

public class DisplayGSI extends DirtyPart implements ISyncPart, ISyncableListener {

	public final IDisplay display;
	// the watched id, followed by how many references it has been attached to, it is loaded by the IDisplayElements
	public Map<InfoUUID, Integer> infoReferences = Maps.newHashMap();
	public Map<Integer, DisplayElementContainer> containers = Maps.newHashMap();
	public List<Integer> changedElements = Lists.newArrayList();
	public SyncableList syncParts = new SyncableList(this);
	public IDisplayElement lookElement = null;

	public static final int EDIT_CONTAINER_ID = 0;

	// click info
	public long lastClickTime;
	public UUID lastClickUUID;

	public SyncTagType.INT container_identity = (INT) new SyncTagType.INT(0).setDefault(-1);
	public SyncTagType.INT display_container_identity_count = (INT) new SyncTagType.INT(1).setDefault(0);
	public SyncTagType.BOOLEAN edit_mode = (BOOLEAN) new SyncTagType.BOOLEAN(2).setDefault(true);
	// private SyncTagType<Double> width = new DOUBLE(2);
	// private SyncTagType<Double> height = new DOUBLE(3);
	// private SyncTagType<Double> scale = new DOUBLE(4);
	private double[] currentScaling;

	/// grid selection mode
	public boolean isGridSelectionMode = false;
	double[] clickPosition1;
	double[] clickPosition2;

	{
		syncParts.addParts(container_identity, display_container_identity_count, edit_mode);// , width, height, scale);
	}

	public DisplayGSI(IDisplay display, int id) {
		this.display = display;
		container_identity.setObject(id);
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) // FIXME
			addEditContainer();
	}

	//// MAIN ACTIONS \\\\

	public boolean onClicked(TileAbstractDisplay part, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (LogisticsHelper.isPlayerUsingOperator(player)) {
			if (!world.isRemote) {
				edit_mode.invert();
				player.sendMessage(new TextComponentTranslation("Edit Mode: " + edit_mode.getObject()));
				display.sendInfoContainerPacket();
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
					if (type.isLeft() || clickPosition1 == null){// FIXME won't need second position everytime|| clickPosition2 == null) {
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

				if (e != null && e.getFirst() instanceof IClickableElement) {
					((IClickableElement) e.getFirst()).onGSIClicked(click, e.getSecond()[0], e.getSecond()[1]);
				}
			}
		}

		return true; // FIXME
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

	public void render() {
		/* DisplayScreenLook look = GSIOverlays.getCurrentLook(this); if (look != null) { Tuple<IDisplayElement, double[]> e = getElementFromXY(look.lookX, look.lookY); if (e != null && e.getFirst() != null && e.getFirst() instanceof ILookableElement) { lookElement = e.getFirst(); } } */
		lookElement = null;

		// renders viewable the display element containers

		if (isGridSelectionMode) {
			GlStateManager.translate(0, 0, -0.005);
			GlStateManager.pushMatrix();

			// render the other containers
			for (DisplayElementContainer container : containers.values()) {
				if (container.canRender() && !isEditContainer(container)) {
					double[] translation = container.getTranslation();
					double[] scaling = container.getMaxScaling();
					DisplayElementHelper.drawRect(translation[0], translation[1], translation[0] + scaling[0], translation[1] + scaling[1], new CustomColour(255, 153, 51).getRGB());
				}
			}

			/// renders the click selections
			if (clickPosition1 != null) {
				GlStateManager.translate(0, 0, -0.001);
				double[] click2 = clickPosition2 == null ? clickPosition1 : clickPosition2;
				double clickStartX = getGridXPosition(Math.min(clickPosition1[0], click2[0]));
				double clickStartY = getGridYPosition(Math.min(clickPosition1[1], click2[1]));
				double clickEndX = Math.max(getDisplayScaling()[0], getGridXPosition(Math.max(clickPosition1[0], click2[0])) + getGridXScale());
				double clickEndY = Math.max(getDisplayScaling()[1], getGridYPosition(Math.max(clickPosition1[1], click2[1])) + getGridYScale());
				DisplayElementHelper.drawRect(clickStartX, clickStartY, clickEndX, clickEndY, new CustomColour(49, 145, 88).getRGB());
			}

			/// render the grid
			GlStateManager.translate(0, 0, -0.001);
			CustomColour green = new CustomColour(174, 227, 227);
			DisplayElementHelper.drawGrid(0, 0, getDisplayScaling()[0], getDisplayScaling()[1], getGridXScale(), getGridYScale(), green.getRGB());

			/// render help overlays
			/*
			GlStateManager.translate(0, 0, -0.001);
			List<String> messages = Lists.newArrayList();
			if (clickPosition1 == null && clickPosition2 == null) {
				messages.add("L-CLICK = SELECT START POSITION");
			} else if (clickPosition1 != null && clickPosition2 == null) {
				messages.add("R-CLICK = SELECT END POSITION");
			} else if (clickPosition1 != null && clickPosition2 != null) {
				messages.add("SHIFT-R = CONFIRM");
			}
			messages.add("SHIFT-L = CANCEL");

			InfoRenderer.renderCenteredStringsWithUniformScaling(messages, getDisplayScaling()[0], getDisplayScaling()[1], 0, 0.75, green.getRGB());
			*/
			GlStateManager.popMatrix();
		} else {
			getViewableContainers().forEach(DisplayElementContainer::render);
		}

	}

	// public double[] offsetClick2(double[] click2) {
	// return ;
	// }

	public double getGridXScale() {
		return Math.max(getDisplayScaling()[0] / 8, display.getDisplayType().width / 4);
	}

	public double getGridYScale() {
		return Math.max(getDisplayScaling()[1] / 8, display.getDisplayType().height / 4);
	}

	public double getGridXPosition(double x) {
		return DisplayElementHelper.toNearestMultiple(x, getDisplayScaling()[0], getGridXScale());
	}

	public double getGridYPosition(double y) {
		return DisplayElementHelper.toNearestMultiple(y, getDisplayScaling()[1], getGridYScale());
	}

	public void startGridSelectionMode() {
		isGridSelectionMode = true;
		clickPosition1 = null;
		clickPosition2 = null;
	}

	public void exitGridSelectionMode() {
		isGridSelectionMode = false;
		clickPosition1 = null;
		clickPosition2 = null;
	}

	public void finishGridSelectionMode() {
		/// FIXME send the clickPositions somewhere
		exitGridSelectionMode();
	}

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
			currentScaling = new double[] { display.getDisplayType().width, display.getDisplayType().height, display.getDisplayType().scale };
		}
	}

	public double[] getDisplayScaling() {
		if (currentScaling == null) {
			updateDisplayScaling();
		}
		return currentScaling;
	}

	public void forEachValidUUID(Consumer<InfoUUID> action) {}

	public boolean isDisplayingUUID(InfoUUID id) {
		return false;
	}

	public void onMonitoredListChanged(InfoUUID uuid, AbstractChangeableList list) {}

	public void onInfoChanged(InfoUUID uuid, IInfo info) {}

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

	//// ELEMENTS \\\\

	public DisplayElementContainer addEditContainer() {
		double[] scaling = new double[] { display.getDisplayType().width / 4, getDisplayScaling()[1], 1 };
		DisplayElementContainer editContainer = new DisplayElementContainer(this, new double[] { 0, 0, 0 }, scaling, 1, EDIT_CONTAINER_ID);
		// editContainer.lock();
		DisplayElementList editList = new DisplayElementList();
		editList.setWidthAlignment(WidthAlignment.LEFT);
		editList.setHeightAlignment(HeightAlignment.TOP);
		editList.getElements().addElement(new ButtonElement(0, 4D, 4D, 2, 15, "GRID MODE") {
			@Override
			public boolean onGSIClicked(DisplayScreenClick click, double subClickX, double subClickY) {
				switch (buttonID) {
				case 0:
					click.gsi.startGridSelectionMode();
					return true;
				}
				return false;
			}
		});
		editList.getElements().addElement(new ButtonElement(1, 4D, 4D, 3, 3, "BANG"));
		editList.getElements().addElement(new ButtonElement(2, 4D, 4D, 1, 5, "CLICK"));
		editList.getElements().addElement(new ButtonElement(3, 4D, 4D, 1, 7, "CRACK"));
		editList.getElements().addElement(new ButtonElement(4, 4D, 4D, 1, 4, "CRACK"));
		editList.getElements().addElement(new ButtonElement(5, 4D, 4D, 2, 7, "CRACK"));
		editList.getElements().addElement(new ButtonElement(6, 4D, 4D, 2, 2, "CRACK"));
		editList.getElements().addElement(new ButtonElement(7, 4D, 4D, 1, 3, "CRACK"));
		editContainer.getElements().addElement(editList);
		containers.put(EDIT_CONTAINER_ID, editContainer);
		return editContainer;
	}

	public DisplayElementContainer getEditContainer() {
		return containers.get(EDIT_CONTAINER_ID);
	}

	public boolean isEditContainer(DisplayElementContainer c) {
		return c.getContainerIdentity() == EDIT_CONTAINER_ID;
	}

	public DisplayElementContainer addElementContainer(double[] translate, double[] scale, double pScale) {
		int identity = createDisplayContainerIdentity();
		DisplayElementContainer container = new DisplayElementContainer(this, translate, scale, pScale, identity);
		containers.put(identity, container);
		return container;
	}

	public void removeElementContainer(int containerID) {
		containers.remove(containerID);
		// reset info references???
	}

	public void addElement(int containerID, IDisplayElement element) {
		containers.get(containerID).getElements().addElement(element);
		addInfoReferences(element.getInfoReferences());
	}

	public void removeElement(int containerID, IDisplayElement element) {
		containers.get(containerID).getElements().removeElement(element);
		removeInfoReferences(element.getInfoReferences());
	}

	/* public NBTTagCompound saveElement(IDisplayElement element, SyncType type) { NBTTagCompound elementTag = new NBTTagCompound(); if (type.isGivenType(SyncType.SAVE) || (type.isGivenType(SyncType.DEFAULT_SYNC) && hasElementChanged(element.getElementIdentity()))) { elementTag.setInteger("identity", element.getElementIdentity()); DisplayElementHelper.saveElement(elementTag, element, type); } return elementTag; } public IDisplayElement loadOrUpdateElement(NBTTagCompound tag, SyncType type) { int identity = tag.getInteger("identity"); IDisplayElement element = elements.get(identity); if(element==null){ element = DisplayElementHelper.loadElement(tag); addElement(element); return element; } return null; } */

	public boolean hasElementChanged(int identityID) {
		return changedElements.contains(identityID);
	}

	public void markElementChanged(int identityID) {
		ListHelper.addWithCheck(changedElements, identityID);
	}

	private int createDisplayContainerIdentity() {
		display_container_identity_count.increaseBy(1);
		return display_container_identity_count.getObject();
	}

	public int getDisplayGSIIdentity() {
		return container_identity.getObject();
	}

	public IDisplay getDisplay() {
		return display;
	}

	//// INFO REFERENCES \\\\

	public void resetInfoReferences() {
		infoReferences.clear();
		// containers.values().forEach(e -> addInfoReferences(e.getInfoReferences())); //FIXME
	}

	public void addInfoReferences(List<InfoUUID> uuid) {
		uuid.forEach(this::addInfoReference);
	}

	public void addInfoReference(InfoUUID uuid) {
		Integer current = infoReferences.get(uuid);
		int value = current == null ? 0 : 1;
		infoReferences.put(uuid, value + 1);
	}

	public void removeInfoReferences(List<InfoUUID> uuid) {
		uuid.forEach(this::removeInfoReference);
	}

	public void removeInfoReference(InfoUUID uuid) {
		int current = infoReferences.get(uuid);
		int newValue = current - 1;
		if (newValue == 0) {
			infoReferences.remove(uuid);
		} else {
			infoReferences.put(uuid, newValue);
		}
	}

	public void updateChangedInfo(List<InfoUUID> uuid) {
		for (DisplayElementContainer e : containers.values()) {
			/* FIXME if(e.getInfoReferences().contains(e)){ e.onElementChanged(); } */
		}
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.SAVE)) {
			NBTTagList tagList = nbt.getTagList("containers", NBT.TAG_COMPOUND);
			tagList.forEach(tag -> loadContainer((NBTTagCompound) tag, type));
		}
		NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
		if (!tag.hasNoTags()) {
			NBTHelper.readSyncParts(tag, type, syncParts);
		}
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
			container = NBTHelper.instanceNBTSyncable(DisplayElementContainer.class, nbt);
			container.gsi = this;
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

	public EnumFacing getFacing() {
		return display.getCableFace();
	}

	public EnumFacing getRotation() {
		return EnumFacing.NORTH; // FIXME - when it's placed set the rotation;
	}
}
