package sonar.logistics.core.tiles.displays.gsi;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.handlers.inventories.containers.ContainerMultipartSync;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ISonarListener;
import sonar.core.network.sync.*;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.api.base.IInfoManager;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.guidance.errors.IInfoError;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.base.requests.colour.GuiColourSelection;
import sonar.logistics.base.utils.LogisticsHelper;
import sonar.logistics.core.tiles.displays.DisplayHandler;
import sonar.logistics.core.tiles.displays.DisplayInfoReferenceHandler;
import sonar.logistics.core.tiles.displays.DisplayViewerHandler;
import sonar.logistics.core.tiles.displays.gsi.gui.GuiEditElementsList;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenLook;
import sonar.logistics.core.tiles.displays.gsi.modes.GSIGridMode;
import sonar.logistics.core.tiles.displays.gsi.modes.GSIModeDefault;
import sonar.logistics.core.tiles.displays.gsi.modes.GSISelectionMode;
import sonar.logistics.core.tiles.displays.gsi.modes.IGSIMode;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIElementPacketHelper;
import sonar.logistics.core.tiles.displays.gsi.render.GSIOverlays;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayGSISaveHandler;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayGSISaveHandler.DisplayGSISavedData;
import sonar.logistics.core.tiles.displays.gsi.storage.EditContainer;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.elements.base.*;
import sonar.logistics.core.tiles.displays.info.types.InfoError;
import sonar.logistics.core.tiles.displays.info.types.text.StyledTextElement;
import sonar.logistics.core.tiles.displays.info.types.text.StyledTitleElement;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;
import sonar.logistics.core.tiles.displays.tiles.holographic.GuiHolographicRescaling;
import sonar.logistics.core.tiles.displays.tiles.holographic.TileAbstractHolographicDisplay;
import sonar.logistics.core.tiles.displays.tiles.holographic.TileAdvancedHolographicDisplay;
import sonar.logistics.core.tiles.displays.tiles.holographic.TileHolographicDisplay;
import sonar.logistics.network.packets.gsi.PacketGSIConnectedDisplayValidate;
import sonar.logistics.network.packets.gsi.PacketGSIInvalidate;
import sonar.logistics.network.packets.gsi.PacketGSISavedDataPacket;
import sonar.logistics.network.packets.gsi.PacketGSIStandardDisplayValidate;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DisplayGSI extends DirtyPart implements ISyncPart, ISyncableListener, IFlexibleGui<IDisplay>, ISonarListener {

	public final IDisplay display;
	public List<IInfoError> errors = new ArrayList<>();
	public List<InfoUUID> references = new ArrayList<>();
	public Map<InfoUUID, IInfo> cachedInfo = new HashMap<>();
	public Map<IDisplayElement, List<IInfoError>> validErrors = new HashMap<>();
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

	public GSIModeDefault default_mode = new GSIModeDefault(this);
	public GSIGridMode grid_mode = new GSIGridMode(this);
	public GSISelectionMode selection_mode = new GSISelectionMode(this);

	public IGSIMode mode = default_mode;

	{
		syncParts.addParts(container_identity, edit_mode);// , width, height, scale);
	}

	public final World world;

	public DisplayGSI(IDisplay display, World world, int id) {
		this.display = display;
		this.world = world;
		container_identity.setObject(id);
		if (!(display instanceof ConnectedDisplay) && world.isRemote) {
			EditContainer.addEditContainer(this);
		}
	}

	//// MAIN ACTIONS \\\\

	public boolean onClicked(TileAbstractDisplay part, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			// only clicks on client side, like a GUI, positioning may not be the same on server
			return true;
		}
		if (display instanceof ConnectedDisplay && !((ConnectedDisplay) display).canBeRendered.getObject()) {
			player.sendMessage(new TextComponentTranslation("THE DISPLAY IS INCOMPLETE"));
			return true;
		}
		if (mode != grid_mode && (type == BlockInteractionType.SHIFT_RIGHT) || LogisticsHelper.isPlayerUsingOperator(player)) {
			GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createEditModePacket(!edit_mode.getObject()), -1, this);
			player.sendMessage(new TextComponentTranslation("Edit Mode: " + !edit_mode.getObject()));
			return true;
		}
		DisplayScreenClick click = getClientClick(DisplayVectorHelper.createClick(player, display, type));
		if(click != null) {
			click.doubleClick = wasDoubleClick(world, player);
			if(mode.renderEditContainer() && isEditContainer(click.clickedContainer) && click.clickedElement != null){
				doDefaultElementClick(part, pos, click, type, player);
				return true;
			}
			if(!mode.onClicked(part, pos, click, type, player) && mode.renderElements() && click.clickedElement != null){
				List<IInfoError> errors = getErrors(click.clickedElement);
				if (errors != null && !errors.isEmpty()) {
					player.sendMessage(new TextComponentTranslation(errors.get(0).getDisplayMessage().get(0)));
					return true;
				}else{
					doDefaultElementClick(part, pos, click, type, player);
				}
			}
		}
		return true; //prevents double clicks?
	}

	public boolean doDefaultElementClick(TileAbstractDisplay part, BlockPos pos, DisplayScreenClick click, BlockInteractionType type, EntityPlayer player) {
		if(click.clickedElement instanceof IClickableElement) {
			int gui = ((IClickableElement) click.clickedElement).onGSIClicked(click, player, click.subClickX, click.subClickY);
			if (gui != -1) {
				requestGui(part, world, pos, player, click.clickedElement.getElementIdentity(), gui, new NBTTagCompound());
			}
			return true;
		}
		return false;
	}

	@Nullable
	public DisplayScreenClick getClientClick(@Nullable DisplayScreenClick click){
		if(click == null){
			return null;
		}
		for (DisplayElementContainer container : containers.values()) {
			if (container.canRender() && container.canClickContainer(click.clickX, click.clickY)) {
				click.clickedContainer = container;
				Tuple<IDisplayElement, double[]> e = container.getElementFromXY(click.clickX, click.clickY);
				if (e != null) {
					click.clickedElement = e.getFirst();
					click.subClickX = e.getSecond()[0];
					click.subClickY = e.getSecond()[1];
					return click;
				}
			}
		}
		return click;
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
		if (lastLookElementUpdate == 0 || (System.currentTimeMillis() - lastLookElementUpdate) > 50) {
			lastLookElementUpdate = System.currentTimeMillis();
			DisplayScreenLook look = GSIOverlays.getCurrentLook(this);
			lookElement = null;
			if (look != null) {
				Tuple<IDisplayElement, double[]> e = getElementFromXY(look.lookX, look.lookY);
				if (e != null && e.getFirst() instanceof ILookableElement) {
					lookElement = e.getFirst();
					lookX = e.getSecond()[0];
					lookY = e.getSecond()[1];
				}
			}
		}
	}

	public void render() {
		//fixes brightness issues when transparent blocks are nearby.
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

		updateLookElement();
		mode.renderMode();
		if(mode.renderElements()){
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
		Vec3d scale = display.getScreenScaling();
		currentScaling = new double[]{scale.x, scale.y, scale.z};
	}

	public double[] getDisplayScaling() {
		if (currentScaling == null) {
			updateDisplayScaling();
		}
		return currentScaling;
	}

	//// INFO REFERENCES \\\\

	public void forEachValidUUID(Consumer<InfoUUID> action) {
		references.forEach(action);
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
			if (e instanceof IInfoReferenceElement) {
				if (((IInfoReferenceElement) e).getInfoReferences().stream().anyMatch(holder -> holder.equals(uuid))) {
					((IInfoReferenceElement) e).onChangeableListChanged(uuid, list);
				}
			}
		});
	}

	public void onInfoChanged(InfoUUID uuid, IInfo info) {
		updateCachedInfo();
		forEachElement(e -> {
			if (e instanceof IInfoReferenceElement) {
				if (((IInfoReferenceElement) e).getInfoReferences().stream().anyMatch(holder -> holder.equals(uuid))) {
					((IInfoReferenceElement) e).onInfoReferenceChanged(uuid, info);
				}
			}
		});
	}

	//// INFO ERRORS \\\\

	public List<IInfoError> getCurrentErrors() {
		return errors;
	}

	public void addInfoError(IInfoError error) {
		if (!getWorld().isRemote) {
			if (ListHelper.addWithCheck(errors, error)) {
                sendInfoContainerPacket(DisplayGSISavedData.ERRORS);
			}
		}
	}

	public void addInfoErrors(List<IInfoError> errors) {
		if (!getWorld().isRemote && !errors.isEmpty()) {
			if (ListHelper.addWithCheck(this.errors, errors)) {
                sendInfoContainerPacket(DisplayGSISavedData.ERRORS);
			}
		}
	}

	public void removeInfoError(IInfoError error) {
		if (!getWorld().isRemote) {
			if (errors.remove(error)) {
                sendInfoContainerPacket(DisplayGSISavedData.ERRORS);
			}
		}
	}

	public void removeInfoErrors(List<IInfoError> errors) {
		if (!getWorld().isRemote && !errors.isEmpty()) {
			errors.clear();
            sendInfoContainerPacket(DisplayGSISavedData.ERRORS);
		}

	}

	public void updateErroredElements() {
		if (errors.isEmpty()) {
			validErrors.clear();
		} else {
			forEachElement(e -> {
				if (e instanceof IInfoReferenceElement && !(e instanceof StyledTitleElement) && !(e instanceof StyledTextElement)) {
					List<IInfoError> errors = getValidErrors(((IInfoReferenceElement) e));
					if (errors != null) {
						validErrors.put(e, errors);
					}
				}

			});
		}
	}

	public boolean isErrored(IDisplayElement e) {
		return validErrors.containsKey(e);
	}

	@Nullable
	public List<IInfoError> getErrors(IDisplayElement e) {
		return validErrors.isEmpty() ? null : validErrors.get(e);
	}

	@Nullable
	public List<IInfoError> getValidErrors(IInfoReferenceElement e) {
		List<InfoUUID> uuids = e.getInfoReferences();
		if (!uuids.isEmpty()) {
			List<IInfoError> validErrors = new ArrayList<>();
			for (IInfoError error : errors) {
				List<InfoUUID> affected = error.getAffectedUUIDs();
				boolean valid = affected.stream().anyMatch(uuids::contains);
				if (valid) {
					validErrors.add(error);
				}
			}
			// FIXME
			return validErrors.isEmpty() ? null : validErrors;
		}
		return null;
	}

	@Nullable
	public IInfoError getValidError(InfoUUID uuid) {
		if (!errors.isEmpty()) {
			for (IInfoError error : errors) {
				if (error.getAffectedUUIDs().contains(uuid)) {
					return error;
				}
			}
		}
		return null;
	}

	public void cleanSavedErrors() {
		if (!getWorld().isRemote) {
			List<IInfoError> oldErrors = new ArrayList<>();
			ERRORS: for (IInfoError error : errors) {
				for (InfoUUID uuid : error.getAffectedUUIDs()) {
					if (references.contains(uuid)) {
						continue ERRORS;
					}
				}
				oldErrors.add(error);
			}
			removeInfoErrors(oldErrors);
		}

	}

	//// INFO REFERENCES \\\\

	public void updateInfoReferences() {
		if (!isValid() || getWorld().isRemote) {
			return;
		}
		List<InfoUUID> newReferences = new ArrayList<>();
		forEachElement(element -> {
			if (element instanceof IInfoReferenceElement) {
				ListHelper.addWithCheck(newReferences, ((IInfoReferenceElement) element).getInfoReferences());
			}
		});
		List<InfoUUID> removed = new ArrayList<>();
		for (InfoUUID ref : references) {
			if (!newReferences.contains(ref)) {
				removed.add(ref);
				continue;
			}
			newReferences.remove(ref);
		}
		if (!newReferences.isEmpty() || !removed.isEmpty()) {
			DisplayInfoReferenceHandler.doInfoReferenceConnect(this, newReferences);
			DisplayInfoReferenceHandler.doInfoReferenceDisconnect(this, removed);
			references.addAll(newReferences);
			references.removeAll(removed);
		}
		cleanSavedErrors();
	}

	public void validateAllInfoReferences() {
		updateInfoReferences();
		DisplayInfoReferenceHandler.doInfoReferenceConnect(this, references);

	}
	
	public void sendConnectedInfo(EntityPlayer player){
		references.forEach(ref -> {
			ILogicListenable listen = ServerInfoHandler.instance().getNetworkTileMap().get(ref.identity);
			if(listen != null){
				listen.getListenerList().addListener(player, ListenerType.TEMPORARY_LISTENER);
			}
		});
	}

	//// CACHED INFO \\\\

	public void updateCachedInfo() {
		IInfoManager manager = PL2.proxy.getInfoManager(world.isRemote);
		Map<InfoUUID, IInfo> newCache = new HashMap<>();
		references.forEach(ref -> newCache.put(ref, manager.getInfoMap().getOrDefault(ref, InfoError.noData)));
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
				InfoPacketHelper.sendLocalProvidersFromScreen(display, world, display.getPos(), player);
				break;
			case 2:

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
			case 2:
				if(obj instanceof TileAbstractHolographicDisplay) {
					TileAbstractHolographicDisplay holographic = (TileAbstractHolographicDisplay) obj;
					return new ContainerMultipartSync(holographic);
				}
				break;
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
			case 2:
				if(obj instanceof TileAdvancedHolographicDisplay){
					TileAdvancedHolographicDisplay holographic = (TileAdvancedHolographicDisplay)obj;
					return new GuiHolographicRescaling(new ContainerMultipartSync(holographic), holographic);
				}
				if(obj instanceof TileHolographicDisplay){
					TileHolographicDisplay holographic = (TileHolographicDisplay)obj;
					return new GuiColourSelection(new ContainerMultipartSync(holographic), holographic, holographic.getScreenColour(), i -> {
						holographic.setScreenColour(i);
						holographic.sendPropertiesToServer();
					});
				}
				break;
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
		return c != null && c.getContainerIdentity() == EDIT_CONTAINER_ID;
	}

	/** creates a new Element Container with the given properties */
	public DisplayElementContainer addElementContainer(double[] translate, double[] scale, double pScale) {
		int identity = createDisplayContainerIdentity();
		DisplayElementContainer container = new DisplayElementContainer(this, translate, scale, pScale, identity);
		containers.put(identity, container);
		validateContainer(container);
        sendInfoContainerPacket(DisplayGSISavedData.ALL_DATA);
		return container;
	}

	/** removes the Element Container with the given id */
	public void removeElementContainer(int containerID) {
		invalidateContainer(containers.get(containerID));
		containers.remove(containerID);
        sendInfoContainerPacket(DisplayGSISavedData.ALL_DATA);
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
		validateElement(e);
		if(!getWorld().isRemote) {
			updateInfoReferences();
			sendInfoContainerPacket(DisplayGSISavedData.ALL_DATA);
		}
	}

	public void onElementRemoved(IElementStorageHolder c, IDisplayElement e) {
		invalidateElement(e);
		if(!getWorld().isRemote) {
			updateInfoReferences();
			sendInfoContainerPacket(DisplayGSISavedData.ALL_DATA);
		}
	}

	public List<EntityPlayerMP> getWatchers(){
	    return DisplayViewerHandler.instance().getWatchingPlayers(this);
    }

    public void forEachWatcher(Consumer<EntityPlayerMP> action){
        getWatchers().forEach(action);
    }

    public List<DisplayGSISavedData> queuedUpdates = new ArrayList<>();

	public void sendInfoContainerPacket(DisplayGSISavedData type) {
		if (world != null && display != null && !world.isRemote) {
			if (!isValid()) {
				return;
			}
            queuedUpdates.add(type);
		}
	}

	public void doQueuedUpdates(){
	    if(!queuedUpdates.isEmpty()) {
            DisplayGSISavedData type = queuedUpdates.size()== 1 ? queuedUpdates.get(0) : DisplayGSISavedData.ALL_DATA;
            forEachWatcher(listener -> PL2.network.sendTo(new PacketGSISavedDataPacket(this, type), listener));
            this.display.onInfoContainerPacket();
			queuedUpdates.clear();
        }
    }

	public void sendValidatePacket(EntityPlayerMP player) {
		if (display instanceof ConnectedDisplay) {
			PL2.network.sendTo(new PacketGSIConnectedDisplayValidate(this, display), player);
		} else if (display instanceof TileAbstractDisplay) {
			PL2.network.sendTo(new PacketGSIStandardDisplayValidate((TileAbstractDisplay) display, this), player);
        }
	}

	public void sendInvalidatePacket(EntityPlayerMP player) {
		PL2.network.sendTo(new PacketGSIInvalidate(this), player);
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
        DisplayGSISaveHandler.readGSIData(this, nbt, type, DisplayGSISavedData.ALL_DATA);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		return DisplayGSISaveHandler.writeGSIData(this, nbt, type, DisplayGSISavedData.ALL_DATA);
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

	public void validateContainer(DisplayElementContainer c) {
		if (c != null && !isEditContainer(c)) {
			c.getElements().forEach(this::validateElement);
		}
	}

	public void invalidateContainer(DisplayElementContainer c) {
		if (c != null && !isEditContainer(c)) {
			c.getElements().forEach(this::invalidateElement);
		}
	}

	public void validateElement(IDisplayElement e) {
		if (e != null && isValid() && !isEditContainer(e.getHolder().getContainer())) {
			e.validate(this);
			PL2.logger.info("Validated Element: " + e.getElementIdentity() + " Client: " + this.getWorld().isRemote);
		}
	}

	public void invalidateElement(IDisplayElement e) {
		if (e != null && !isEditContainer(e.getHolder().getContainer())) {
			e.invalidate(this);
			PL2.logger.info("Invalidated Element: " + e.getElementIdentity() + " Client: " + this.getWorld().isRemote);
		}
	}

	public boolean isValid = false;

	@Override
	public boolean isValid() {
		return isValid;
	}

	public void validate() {
		if (!isValid) {
			isValid = true;
			if (!world.isRemote) {
				PL2.proxy.getServerManager().gsiMap.put(getDisplayGSIIdentity(), this);
				references.clear();
                updateInfoReferences();
                updateCachedInfo();
                updateScaling();
				if (display instanceof ConnectedDisplay) {
					DisplayHandler.updateWatchers(Lists.newArrayList(), (ConnectedDisplay) display);
				}
				forEachWatcher(this::sendValidatePacket);
			} else {
				PL2.proxy.getClientManager().getGSIMap().put(getDisplayGSIIdentity(), this);
                updateCachedInfo();
                updateScaling();
			}
			forEachElement(this::validateElement);

            display.onGSIValidate();
			PL2.logger.info("Validated GSI: " + this.getDisplayGSIIdentity() + " Client: " + this.getWorld().isRemote);
		}
	}

	public void invalidate() {
		if (isValid) {
			isValid = false;
			if (!world.isRemote) {
				PL2.proxy.getServerManager().gsiMap.remove(getDisplayGSIIdentity());
                DisplayInfoReferenceHandler.doInfoReferenceDisconnect(this, references);
				forEachWatcher(this::sendInvalidatePacket);
			} else {
				PL2.proxy.getClientManager().getGSIMap().remove(getDisplayGSIIdentity());
			}
			forEachElement(this::invalidateElement);

            display.onGSIInvalidate();
			PL2.logger.info("Invalidated GSI: " + this.getDisplayGSIIdentity() + " Client: " + this.getWorld().isRemote);
		}
	}
}
