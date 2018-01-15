package sonar.logistics.common.multiparts.readers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.IMonitoredValueInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.TileSidedLogistics;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredEntity;
import sonar.logistics.network.sync.SyncMonitoredType;
import sonar.logistics.networking.PL2ListenerList;

public abstract class TileAbstractReader<T extends IInfo> extends TileSidedLogistics implements INetworkReader<T>, IByteBufTile, IFlexibleGui {

	public static final int ADD = -9, PAIRED = -10, ALL = 100;

	public final PL2ListenerList listeners = new PL2ListenerList(this, ListenerType.ALL.size());
	public final ChannelList list = new ChannelList(getIdentity(), this.channelType(), -2);
	protected List<INetworkListHandler> validHandlers = null;
	public SyncMonitoredType<T> selectedInfo = new SyncMonitoredType<T>(-5);
	public SyncTagType.BOOLEAN hasMonitor = new SyncTagType.BOOLEAN(-4);
	public int lastPos = -1;

	public SyncUUID lastSelectedUUID = new SyncUUID(-10);
	public SyncCoords lastSelected = new SyncCoords(-11);
	{
		syncList.addParts(list, hasMonitor);
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return dir == this.getCableFace() ? CableRenderType.HALF // internal
				: CableRenderType.CABLE; // external
	}

	@Override
	public NetworkConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? NetworkConnectionType.NETWORK : toCheck == getCableFace().getOpposite() ? NetworkConnectionType.VISUAL : NetworkConnectionType.NONE;
	}

	public abstract List<INetworkListHandler> addValidHandlers(List<INetworkListHandler> handlers);

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

	public boolean canMonitorInfo(IMonitoredValue<T> info, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<T>> channels, List<NodeConnection> usedChannels) {
		return true;
	}

	@Override
	public List<INetworkListHandler> getValidHandlers() {
		if (validHandlers == null) {
			validHandlers = addValidHandlers(Lists.newArrayList());
		}
		return validHandlers;
	}

	@Nullable
	public AbstractChangeableList<T> getMonitoredList() {
		InfoUUID id = new InfoUUID(getIdentity(), 0);
		return PL2.getInfoManager(world.isRemote).getMonitoredList(id);
	}

	public int getMaxInfo() {
		return 4;
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
	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			hasMonitor.setObject(CableHelper.getDisplay(world, this.getPos(), EnumDisplayFaceSlot.fromFace(getCableFace())) != null);
		}
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
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
		case -4:
			lastSelectedUUID.readFromBuf(buf);
			list.give(lastSelectedUUID.getUUID());
			sendByteBufPacket(1);
			break;
		case -3:
			lastSelected.readFromBuf(buf);
			list.give(lastSelected.getCoords());
			sendByteBufPacket(1);
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
