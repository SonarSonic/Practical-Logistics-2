package sonar.logistics.core.tiles.readers.base;

import io.netty.buffer.ByteBuf;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.lists.UniversalChangeableList;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.api.core.tiles.readers.INetworkReader;
import sonar.logistics.api.core.tiles.readers.channels.INetworkChannels;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.channels.ChannelList;
import sonar.logistics.base.channels.ChannelType;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.base.listeners.PL2ListenerList;
import sonar.logistics.base.utils.slots.EnumDisplayFaceSlot;
import sonar.logistics.core.tiles.base.TileSidedLogistics;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredEntity;
import sonar.logistics.network.sync.SyncMonitoredType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public abstract class TileAbstractReader<T extends IInfo> extends TileSidedLogistics implements INetworkReader<T>, IByteBufTile, IFlexibleGui {

	public static final int ADD = -9, PAIRED = -10, ALL = 100;

	public PL2ListenerList listeners = new PL2ListenerList(this, ListenerType.ALL.size());
	public ChannelList list = new ChannelList(getIdentity(), this.channelType(), -2);
	protected List<INetworkHandler> validHandlers = null;
	public SyncMonitoredType<T> selectedInfo = new SyncMonitoredType<>(-5);
	public SyncTagType.BOOLEAN hasMonitor = new SyncTagType.BOOLEAN(-4);
	public int lastPos = -1;

	public SyncUUID lastSelectedUUID = new SyncUUID(-10);
	public SyncCoords lastSelected = new SyncCoords(-11);
	{
		syncList.addParts(list, hasMonitor);
	}

	@Override
	public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
		return dir == this.getCableFace() ? EnumCableRenderSize.HALF // internal
				: EnumCableRenderSize.CABLE; // external
	}

	@Override
	public EnumCableConnection canConnect(int registryID, EnumCableConnectionType type, EnumFacing dir, boolean internal) {
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? EnumCableConnection.NETWORK : toCheck == getCableFace().getOpposite() ? EnumCableConnection.VISUAL : EnumCableConnection.NONE;
	}

	public abstract List<INetworkHandler> addValidHandlers(List<INetworkHandler> handlers);

	//// ILogicMonitor \\\\

	@Override
	public AbstractChangeableList<T> getViewableList(AbstractChangeableList<T> updateList, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<T>> channels, List<NodeConnection> usedChannels) {
		ChannelList readerChannels = getChannels();
		for (Entry<NodeConnection, AbstractChangeableList<T>> entry : channels.entrySet()) {
			if (readerChannels.isMonitored(entry.getKey())) {
				for (IMonitoredValue<T> coordInfo : entry.getValue().getList()) {
					if (canMonitorInfo(coordInfo, uuid, channels, usedChannels)) {
						updateList.add(coordInfo.getSaveableInfo());
					}
				}
				usedChannels.add(entry.getKey());
				if (channelType() == ChannelType.SINGLE) {
					break;
				}
			}
		}
		return updateList;
	}

	public List<NodeConnection> getUsedChannels(Map<NodeConnection, AbstractChangeableList<T>> channels) {
		List<NodeConnection> usedChannels = new ArrayList<>();
		ChannelList readerChannels = getChannels();
		for (Entry<NodeConnection, AbstractChangeableList<T>> entry : channels.entrySet()) {
			if (readerChannels.isMonitored(entry.getKey())) {
				usedChannels.add(entry.getKey());
			}
		}
		return usedChannels;
	}

	public boolean canMonitorInfo(IMonitoredValue<T> info, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<T>> channels, List<NodeConnection> usedChannels) {
		return true;
	}

	@Override
	public List<INetworkHandler> getValidHandlers() {
		if (validHandlers == null) {
			validHandlers = addValidHandlers(new ArrayList<>());
		}
		return validHandlers;
	}

	@Nullable
	public AbstractChangeableList<T> getMonitoredList() {
		InfoUUID id = new InfoUUID(getIdentity(), 0);
		return PL2.proxy.getInfoManager(isClient()).getChangeableListMap().getOrDefault(id, UniversalChangeableList.newChangeableList());
	}

	//// IChannelledTile \\\\

	@Override
	public ChannelList getChannels() {
		return list;
	}

	public void sendCoordsToServer(IInfo info, int channelID) {
		if (info instanceof MonitoredBlockCoords) {
			lastSelected.setCoords(((MonitoredBlockCoords) info).getCoords());
			sendByteBufPacket(-3);
		}
		if (info instanceof MonitoredEntity) {
			lastSelectedUUID.setObject(((MonitoredEntity) info).getUUID());
			sendByteBufPacket(-4);
		}
	}

	//// ILogicViewable \\\\

	public PL2ListenerList getListenerList() {
		return listeners;
	}

	@Override
	public void onListenerAdded(ListenerTally<PlayerListener> tally) {
		super.onListenerAdded(tally);
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, tally.listener.player);
	}

	//// IOperatorProvider \\\\
	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Channels Configured: " + !list.hasChannels());
		info.add("Max Info: " + getMaxInfo());
		// info.add("UUID: " + getIdentity());
	}

	//// EVENTS \\\\
	public void markChanged(IDirtyPart part) {
		super.markChanged(part);
		if (part == list) {
			for (INetworkHandler handler : getValidHandlers()) {
				INetworkChannels channels = network.getNetworkChannels(handler.getChannelsType());
				if (channels != null) {
					channels.onChannelsChanged();
				}
			}
		}
	}

	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			Optional<IMultipartTile> display = info == null ? Optional.empty() : info.getContainer().getPartTile(EnumDisplayFaceSlot.fromFace(getCableFace()));
			hasMonitor.setObject(display.isPresent() && display.get() instanceof IDisplay);
			SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
		}
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case -5:
			list.writeToBuf(buf);
			break;
		case -4:
			lastSelectedUUID.writeToBuf(buf);
			break;
		case -3:
			lastSelected.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case -5:
			list.readFromBuf(buf);
			break;
		case -4:
			lastSelectedUUID.readFromBuf(buf);
			list.give(lastSelectedUUID.getUUID());
			//TODO
			//List<PlayerListener> listeners = getListenerList().getAllListeners(ListenerType.NEW_GUI_LISTENER, ListenerType.OLD_GUI_LISTENER);
			//listeners.forEach(listener -> SonarMultipartHelper.sendMultipartSyncToPlayer(this, listener.player));
			sendByteBufPacket(-5);
			break;

		case -3:
			lastSelected.readFromBuf(buf);
			list.give(lastSelected.getCoords());
			//listeners = getListenerList().getAllListeners(ListenerType.NEW_GUI_LISTENER, ListenerType.OLD_GUI_LISTENER);
			//listeners.forEach(listener -> SonarMultipartHelper.sendMultipartSyncToPlayer(this, listener.player));
			sendByteBufPacket(-5);
			break;
		}
	}

	//// GUI \\\\

	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 1:
			getNetwork().sendConnectionsPacket(player);
			break;
		}
	}
}
