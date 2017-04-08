package sonar.logistics.api.displays;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncableList;
import sonar.core.utils.IUUIDIdentity;
import sonar.logistics.PL2;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IConnectable;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.cabling.NetworkConnectionType;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.generic.ScreenMultipart;
import sonar.logistics.network.PacketConnectedDisplayScreen;

/** used with Large Display Screens so they all have one uniform InfoContainer, Viewer list etc. */
public class ConnectedDisplayScreen implements IInfoDisplay, IConnectable, INBTSyncable, IScaleableDisplay, ISyncPart {

	public ListenerList<PlayerListener> listeners = new ListenerList(this, ListenerType.ALL.size());
	private int registryID = -1;
	public ILargeDisplay topLeftScreen = null;
	public SyncableList syncParts = new SyncableList(this);
	public SyncEnum<EnumFacing> face = new SyncEnum(EnumFacing.VALUES, 0);
	public SyncEnum<ScreenLayout> layout = new SyncEnum(ScreenLayout.values(), 1);
	public SyncTagType.INT width = new SyncTagType.INT(2), height = new SyncTagType.INT(3);
	public SyncTagType.BOOLEAN canBeRendered = new SyncTagType.BOOLEAN(4);
	public InfoContainer container = new InfoContainer(this);
	public SyncCoords topLeftCoords = new SyncCoords(5);
	public SyncTagType.BOOLEAN isLocked = new SyncTagType.BOOLEAN(6);
	// public double[] scaling = null;
	public boolean hasChanged = true;
	public boolean sendViewers;

	// server side
	public ArrayList<ILargeDisplay> displays = new ArrayList(); // cached

	{
		syncParts.addParts(face, layout, width, height, canBeRendered, topLeftCoords, container, isLocked);
	}

	public ConnectedDisplayScreen(ILargeDisplay display) {
		registryID = display.getRegistryID();
		face.setObject(display.getCableFace());
		this.hasChanged = true;
	}

	public ConnectedDisplayScreen(int registryID) {
		this.registryID = registryID;
		this.hasChanged = true;
	}
	
	public void lock(){
		isLocked.setObject(true);
		PL2.getDisplayManager().lockedIDs.add(registryID);
	}
	
	public void unlock(){
		isLocked.setObject(false);
		PL2.getDisplayManager().lockedIDs.remove(registryID);		
	}

	public void update(int registryID) {
		if (sendViewers) {
			sendViewers();
		}
		if (hasChanged || this.registryID != registryID) {
			this.registryID = registryID;
			displays = PL2.getDisplayManager().getConnections(registryID);
			if (!displays.isEmpty()) {
				if (!displays.get(0).getCoords().getWorld().isRemote) {
					setDisplayScaling(displays.get(0), displays);
				}
			}
			hasChanged = false;
			sendViewers = true;
		}
	}

	public void sendViewers() {
		ArrayList<PlayerListener> listeners = getListenerList().getListeners(ListenerType.INFO, ListenerType.FULL_INFO);
		if (!listeners.isEmpty()) {
			listeners.forEach(listener -> PL2.network.sendTo(new PacketConnectedDisplayScreen(this, registryID), listener.player));
			sendViewers = false;
		} else {
			sendViewers = true;
		}
	}

	public void setDisplayScaling(ILargeDisplay primary, ArrayList<ILargeDisplay> displays) {
		displays.forEach(display -> display.setConnectedDisplay(this)); // make sure to read the NBT first so WIDTH and HEIGHT arn't altered

		BlockCoords primaryCoords = primary.getCoords();
		int minX = primaryCoords.getX();
		int maxX = primaryCoords.getX();
		int minY = primaryCoords.getY();
		int maxY = primaryCoords.getY();
		int minZ = primaryCoords.getZ();
		int maxZ = primaryCoords.getZ();

		EnumFacing meta = primary.getCableFace();
		boolean north = meta == EnumFacing.NORTH;
		for (ILargeDisplay display : displays) {
			BlockCoords coords = display.getCoords();

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
				switch (primary.getRotation()) {
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
				switch (primary.getRotation()) {
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
					IInfoDisplay display = LogisticsAPI.getCableHelper().getDisplayScreen(coords, meta);
					if (display == null || !(display instanceof ILargeDisplay)) {
						this.canBeRendered.setObject(false);
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
	}

	public ArrayList<ILogicViewable> getLogicMonitors(ArrayList<ILogicViewable> monitors) {
		displays = PL2.getDisplayManager().getConnections(registryID);
		for (ILargeDisplay display : displays) {
			if (display instanceof ScreenMultipart) {
				monitors = PL2.getServerManager().getViewables(monitors, (ScreenMultipart) display);
			}
		}
		return monitors;
	}

	public void setHasChanged() {
		hasChanged = true;
	}

	public void setTopLeftScreen(ILargeDisplay display, boolean isTopLeft) {
		if (isTopLeft) {
			topLeftScreen = display;
			this.topLeftCoords.setCoords(display.getCoords());
			display.setShouldRender(true);
			face.setObject(display.getCableFace());
			if (!display.getCoords().getWorld().isRemote)
				PL2.getServerManager().addDisplay(display);
		} else {
			display.setShouldRender(false);
			if (!display.getCoords().getWorld().isRemote)
				PL2.getServerManager().removeDisplay(display);
		}

	}

	@Override
	public IInfoContainer container() {
		return container;
	}

	@Override
	public ScreenLayout getLayout() {
		return layout.getObject();
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.LARGE;
	}

	@Override
	public int maxInfo() {
		return topLeftScreen != null ? topLeftScreen.maxInfo() : 4;
	}

	@Override
	public EnumFacing getCableFace() {
		return topLeftScreen != null ? topLeftScreen.getCableFace() : EnumFacing.NORTH;
	}

	@Override
	public NetworkConnectionType canConnect(EnumFacing dir) {
		return NetworkConnectionType.NETWORK;
	}

	@Override
	public BlockCoords getCoords() {
		return topLeftScreen != null ? topLeftScreen.getCoords() : null;
	}

	@Override
	public int getNetworkID() {
		return topLeftScreen != null ? topLeftScreen.getNetworkID() : -1;
	}

	@Override
	public ConnectableType getCableType() {
		return ConnectableType.CONNECTION;
	}

	@Override
	public void addToNetwork() {
	}

	@Override
	public void removeFromNetwork() {
	}

	@Override
	public int getRegistryID() {
		return registryID;
	}

	@Override
	public void setRegistryID(int id) {
		this.registryID = id;
		this.hasChanged = true;
	}

	@Override
	public boolean canConnectOnSide(int connectingID, EnumFacing dir) {
		return true;
	}

	public void readData(NBTTagCompound nbt, SyncType type) {
		if (nbt.hasKey(this.getTagName())) {
			NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
			NBTHelper.readSyncParts(tag, type, this.syncParts);
			// layout.readData(tag, type);
			container.resetRenderProperties();
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = new NBTTagCompound();
		NBTHelper.writeSyncParts(tag, type, this.syncParts, true);
		// layout.writeData(tag, type);
		if (!tag.hasNoTags())
			nbt.setTag(this.getTagName(), tag);
		return nbt;
	}

	public ILargeDisplay getTopLeftScreen() {
		if (topLeftCoords.getCoords() != null) {
			IInfoDisplay display = LogisticsAPI.getCableHelper().getDisplayScreen(topLeftCoords.getCoords(), face.getObject());
			if (display instanceof ILargeDisplay)
				this.topLeftScreen = (ILargeDisplay) display;
		}
		return topLeftScreen;
	}

	@Override
	public double[] getScaling() {
		double max = Math.min(this.height.getObject().intValue() + 1.3, this.width.getObject().intValue() + 1);
		return new double[] { this.getDisplayType().width + this.width.getObject().intValue(), this.getDisplayType().height + this.height.getObject().intValue(), max / 100 };
	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, this.writeData(new NBTTagCompound(), SyncType.SAVE));
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		readData(ByteBufUtils.readTag(buf), SyncType.SAVE);
		container.resetRenderProperties();
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
		return this.getTopLeftScreen();
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
		if (this.getTopLeftScreen() != null) {
			this.getTopLeftScreen().markChanged(this);
		}
	}

	public ListenerList<PlayerListener> getListenerList() {
		return listeners;
	}

	@Override
	public int getIdentity() {
		return -1;
	}

	@Override
	public EnumFacing getRotation() {
		return getTopLeftScreen() == null ? EnumFacing.NORTH : getTopLeftScreen().getRotation();
	}

	@Override
	public ILogisticsNetwork getNetwork() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onNetworkConnect(ILogisticsNetwork network) {}

	@Override
	public void onNetworkDisconnect(ILogisticsNetwork network) {}

	@Override
	public UUID getUUID() {
		return getTopLeftScreen() == null ? IUUIDIdentity.INVALID_UUID : getTopLeftScreen().getUUID();
	}

	@Override
	public void onListenerAdded(ListenerTally<PlayerListener> tally) {}

	@Override
	public void onListenerRemoved(ListenerTally<PlayerListener> tally) {}
}