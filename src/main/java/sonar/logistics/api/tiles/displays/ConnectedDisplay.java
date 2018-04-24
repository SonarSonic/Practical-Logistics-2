package sonar.logistics.api.tiles.displays;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncableList;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.states.ErrorMessage;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.networking.cabling.CableHelper;
import sonar.logistics.networking.displays.ConnectedDisplayChange;
import sonar.logistics.networking.displays.DisplayHandler;
import sonar.logistics.networking.displays.DisplayHelper;
import sonar.logistics.worlddata.ConnectedDisplayData;

/** used with Large Display Screens so they all have one uniform InfoContainer, Viewer list etc. */
public class ConnectedDisplay implements IDisplay, INBTSyncable, IScaleableDisplay, ISyncPart, ISyncableListener {

	public int dim;
	public int registryID;
	public ILargeDisplay topLeftScreen = null;
	public SyncableList syncParts = new SyncableList(this);
	public SyncEnum<EnumFacing> face = new SyncEnum(EnumFacing.VALUES, 0);
	public SyncEnum<DisplayLayout> layout = new SyncEnum(DisplayLayout.values(), 1);
	public SyncTagType.INT width = new SyncTagType.INT(2), height = new SyncTagType.INT(3);
	public SyncTagType.BOOLEAN canBeRendered = (BOOLEAN) new SyncTagType.BOOLEAN(4).setDefault(true);
	public DisplayGSI gsi = null;
	public SyncCoords topLeftCoords = new SyncCoords(5);
	public SyncTagType.BOOLEAN isLocked = new SyncTagType.BOOLEAN(6);
	public World world;

	// server side
	{
		syncParts.addParts(face, layout, width, height, canBeRendered, topLeftCoords, isLocked);
	}

	ConnectedDisplay() {}

	public static ConnectedDisplay loadDisplay(World world, int registryID) {
		NBTTagCompound tag = world.isRemote ? null : ConnectedDisplayData.unloadedDisplays.get(registryID);
		ConnectedDisplay display = new ConnectedDisplay();
		display.world = world;
		display.gsi = new DisplayGSI(display, world, registryID);
		display.registryID = registryID;
		if (tag != null) {
			display.readData(tag, SyncType.SAVE);
			ConnectedDisplayData.unloadedDisplays.remove(registryID);
		}
		return display;
	}

	/* public ConnectedDisplay(ILargeDisplay display, World world) { registryID = display.getRegistryID(); face.setObject(display.getCableFace()); this.world = world; } public ConnectedDisplay(World world, int registryID) { if (registryID == -1) { PL2.logger.info("DISPLAY CREATED WITH ID -1"); } this.registryID = registryID; this.world = world; } */

	public void setDisplayScaling() {
		List<ILargeDisplay> displays = DisplayHandler.instance().getConnections(getRegistryID());
		displays.forEach(display -> display.setConnectedDisplay(this)); // make sure to read the NBT first so WIDTH and HEIGHT arn't altered

		boolean init = false;
		int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;

		EnumFacing meta = getCableFace();
		boolean north = meta == EnumFacing.NORTH;
		for (ILargeDisplay display : displays) {
			BlockCoords coords = display.getCoords();
			if (!init) {
				init = true;
				minX = maxX = coords.getX();
				minY = maxY = coords.getY();
				minZ = maxZ = coords.getZ();
				continue;
			}
			if (coords.getX() > maxX) {
				maxX = coords.getX();
			} else if (coords.getX() < minX) {
				minX = coords.getX();
			}
			if (coords.getY() > maxY) {
				maxY = coords.getY();
			} else if (coords.getY() < minY) {
				minY = coords.getY();
			}
			if (coords.getZ() > maxZ) {
				maxZ = coords.getZ();
			} else if (coords.getZ() < minZ) {
				minZ = coords.getZ();
			}
		}
		switch (meta.getAxis()) {
		case X:
			this.width.setObject(maxZ - minZ);
			this.height.setObject(maxY - minY);
			break;
		case Y:
			this.width.setObject(maxX - minX);
			this.height.setObject(maxZ - minZ);
			if (meta == EnumFacing.UP) {
				switch (getGSI().getRotation()) {
				case DOWN:
					break;
				case EAST:
					int newX = maxX;
					maxX = minX;
					minX = newX;
					break;
				case NORTH:
					break;
				case SOUTH:
					newX = maxX;
					maxX = minX;
					minX = newX;

					int newZ = maxZ;
					maxZ = minZ;
					minZ = newZ;
					break;
				case UP:
					break;
				case WEST:
					newZ = maxZ;
					maxZ = minZ;
					minZ = newZ;
					break;
				default:
					break;

				}
			} else if (meta == EnumFacing.DOWN) {
				switch (getGSI().getRotation()) {
				case DOWN:
					break;
				case EAST:
					int newX = maxX;
					maxX = minX;
					minX = newX;
					int newZ = maxZ;
					maxZ = minZ;
					minZ = newZ;
					break;
				case NORTH:
					newX = maxX;
					maxX = minX;
					minX = newX;
					break;
				case SOUTH:
					newZ = maxZ;
					maxZ = minZ;
					minZ = newZ;
					break;
				case UP:
					break;
				case WEST:
					break;
				default:
					break;

				}
			}
			break;
		case Z:
			this.width.setObject(maxX - minX);
			this.height.setObject(maxY - minY);
			break;
		default:
			break;
		}

		for (int x = Math.min(minX, maxX); x <= Math.max(minX, maxX); x++) {
			for (int y = Math.min(minY, maxY); y <= Math.max(minY, maxY); y++) {
				for (int z = Math.min(minZ, maxZ); z <= Math.max(minZ, maxZ); z++) {
					BlockCoords coords = new BlockCoords(x, y, z);
					IDisplay display = CableHelper.getDisplay(world, coords.getBlockPos(), EnumDisplayFaceSlot.fromFace(meta));
					if (!(display instanceof ILargeDisplay)) {
						canBeRendered.setObject(false);
						return;
					}
					AxisDirection dir = meta.getAxisDirection();
					if (meta.getAxis() != Axis.Z) {
						dir = dir == AxisDirection.POSITIVE ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE;
					}
					boolean isTopLeft = (dir == AxisDirection.POSITIVE && x == minX && y == maxY && z == minZ) || (dir == AxisDirection.NEGATIVE && x == maxX && y == maxY && z == maxZ);
					setTopLeftScreen((ILargeDisplay) display, isTopLeft);
				}
			}
		}
		this.canBeRendered.setObject(true);
		gsi.updateScaling();
	}

	public List<ILogicListenable> getLocalProviders(List<ILogicListenable> monitors) {
		List<ILargeDisplay> displays = DisplayHandler.instance().getConnections(registryID);
		for (ILargeDisplay display : displays) {
			if (display instanceof TileAbstractDisplay) {
				monitors = DisplayHelper.getLocalProvidersFromDisplay(monitors, ((TileAbstractDisplay) display).getWorld(), ((TileAbstractDisplay) display).getPos(), display);
			}
		}
		return monitors;
	}

	private void setHasChanged() {
		DisplayHandler.instance().markConnectedDisplayChanged(getRegistryID(), ConnectedDisplayChange.WATCHERS_CHANGED);
	}

	public void setTopLeftScreen(ILargeDisplay display, boolean isTopLeft) {
		if (isTopLeft) {
			topLeftScreen = display;
			this.topLeftCoords.setCoords(display.getCoords());
			display.setShouldRender(true);
			face.setObject(display.getCableFace());
		} else {
			display.setShouldRender(false);
		}

	}

	@Override
	public DisplayGSI getGSI() {
		return gsi;
	}

	public void setGSI(DisplayGSI gsi) {
		this.gsi = gsi;
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.LARGE;
	}

	@Override
	public EnumFacing getCableFace() {
		return face.getObject();
	}

	@Override
	public BlockCoords getCoords() {
		return getTopLeftScreen() != null ? topLeftScreen.getCoords() : topLeftCoords.getCoords();
	}

	public int getDimension() {
		return getCoords().getDimension();
	}

	@Override
	public int getInfoContainerID() {
		return getRegistryID();
	}

	public void onInfoContainerPacket() {
		DisplayHandler.instance().markConnectedDisplayChanged(getRegistryID(), ConnectedDisplayChange.WATCHERS_CHANGED);
	}

	public int getRegistryID() {
		return registryID;
	}

	public void readData(NBTTagCompound nbt, SyncType type) {
		if (nbt.hasKey(getTagName())) {
			NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
			NBTHelper.readSyncParts(tag, type, this.syncParts);
			getGSI().readData(tag, type);
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = new NBTTagCompound();
		NBTHelper.writeSyncParts(tag, type, this.syncParts, true);
		getGSI().writeData(tag, type);
		if (!tag.hasNoTags()) {
			nbt.setTag(this.getTagName(), tag);
		}
		return nbt;
	}

	public ILargeDisplay getActualDisplay() {
		return getTopLeftScreen();
	}

	public ILargeDisplay getTopLeftScreen() {
		if (topLeftCoords.getCoords() != null) {
			IDisplay display = CableHelper.getDisplay(world, topLeftCoords.getCoords().getBlockPos(), EnumDisplayFaceSlot.fromFace(face.getObject()));
			if (display instanceof ILargeDisplay)
				this.topLeftScreen = (ILargeDisplay) display;
		}
		return topLeftScreen;
	}

	@Override
	public double[] getScaling() {
		double max = Math.min(this.height.getObject() + 1.3, this.width.getObject() + 1);
		return new double[] { this.getDisplayType().width + this.width.getObject(), this.getDisplayType().height + this.height.getObject(), max / 100 };
	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, writeData(new NBTTagCompound(), SyncType.SAVE));
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		readData(ByteBufUtils.readTag(buf), SyncType.SAVE);
	}

	@Override
	public boolean canSync(SyncType sync) {
		return SyncType.isGivenType(sync, SyncType.DEFAULT_SYNC, SyncType.SAVE);
	}

	@Override
	public String getTagName() {
		return "connected";
	}

	@Override
	public ISyncableListener getListener() {
		return this;
	}

	@Override
	public IDirtyPart setListener(ISyncableListener listener) {
		if (listener instanceof ILargeDisplay) {
			if (listener != topLeftScreen) {
				setTopLeftScreen((ILargeDisplay) listener, true);
			}
		}
		return this;
	}

	@Override
	public void markChanged(IDirtyPart part) {
		syncParts.markSyncPartChanged(part);
		DisplayHandler.instance().markConnectedDisplayChanged(getRegistryID(), ConnectedDisplayChange.WATCHERS_CHANGED);
	}

	@Override
	public int getNetworkID() {
		return getTopLeftScreen().getNetworkID();
	}

	@Override
	public ILogisticsNetwork getNetwork() {
		return getTopLeftScreen().getNetwork();
	}

	@Override
	public int getIdentity() {
		return registryID;// getTopLeftScreen().getIdentity();
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return new ErrorMessage[0];
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.LARGE_DISPLAY_SCREEN;
	}

	@Override
	public void onNetworkConnect(ILogisticsNetwork network) {}

	@Override
	public void onNetworkDisconnect(ILogisticsNetwork network) {}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		return CableConnectionType.NETWORK;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.CABLE;
	}

	@Override
	public World getActualWorld() {
		return gsi.getWorld();
	}

	@Override
	public World getPartWorld() {
		return getTopLeftScreen().getPartWorld();
	}

}