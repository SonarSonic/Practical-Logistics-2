package sonar.logistics.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.utils.Pair;
import sonar.logistics.Logistics;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.displays.IInfoContainer;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IEntityMonitorHandler;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.ILogicMonitor;
import sonar.logistics.api.readers.IdentifiedCoordsList;
import sonar.logistics.api.viewers.IViewersList;
import sonar.logistics.api.viewers.ViewerTally;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.network.PacketMonitoredCoords;
import sonar.logistics.network.PacketMonitoredList;

public abstract class AbstractNetwork implements ILogisticsNetwork {

	public boolean resendAllLists = false;
	public final Map<IEntityMonitorHandler, Map<Entity, MonitoredList<?>>> entityConnectionInfo = new LinkedHashMap();
	public final Map<ITileMonitorHandler, Map<NodeConnection, MonitoredList<?>>> tileConnectionInfo = new LinkedHashMap(); // block coords stored with the info gathered
	public final Map<LogicMonitorHandler, Map<ILogicMonitor, MonitoredList<?>>> monitorInfo = new LinkedHashMap();
	public final Map<IInfoDisplay, IInfoContainer> connectedDisplays = new LinkedHashMap();
	public final ArrayList<ILogicMonitor> localMonitors = new ArrayList();

	/** adds an info display to the list of display associated with this cache */
	public void addDisplay(IInfoDisplay display) {
		if (!connectedDisplays.containsKey(display)) {
			connectedDisplays.put(display, new InfoContainer(display));
		}
	}

	public void removeDisplay(IInfoDisplay display) {
		connectedDisplays.remove(display);
	}

	public <T extends IMonitorInfo> void addMonitor(ILogicMonitor<T> monitor) {
		monitorInfo.putIfAbsent(monitor.getHandler(), new LinkedHashMap());
		if (!monitorInfo.get(monitor.getHandler()).containsKey(monitor)) {
			monitorInfo.get(monitor.getHandler()).put(monitor, MonitoredList.<T>newMonitoredList(getNetworkID()));
		}
		compileConnectionList(monitor.getHandler());
	}

	public <T extends IMonitorInfo> void removeMonitor(ILogicMonitor<T> monitor) {
		monitorInfo.get(monitor.getHandler()).remove(monitor);
		compileConnectionList(monitor.getHandler());
	}


	public <T extends IMonitorInfo> MonitoredList<T> updateMonitoredList(ILogicMonitor<T> monitor, int infoID, Map<NodeConnection, MonitoredList<?>> connections, Map<Entity, MonitoredList<?>> entityConnections, ArrayList<NodeConnection> nodeConnections, ArrayList<Entity> entities) {
		MonitoredList<T> updateList = MonitoredList.<T>newMonitoredList(getNetworkID());
		IdentifiedCoordsList channels = monitor.getChannels(infoID); // TODO
		for (Entry<NodeConnection, MonitoredList<?>> entry : connections.entrySet()) {
			if ((entry.getValue() != null && !entry.getValue().isEmpty()) && (channels.isEmpty() || channels.contains(entry.getKey().coords))) {
				for (T coordInfo : (MonitoredList<T>) entry.getValue()) {
					updateList.addInfoToList(coordInfo, (MonitoredList<T>) entry.getValue());
				}
				updateList.sizing.add(entry.getValue().sizing);
				nodeConnections.add(entry.getKey());
				if (monitor.channelType() == ChannelType.SINGLE) {
					break;
				}
			}
		}
		for (Entry<Entity, MonitoredList<?>> entry : entityConnections.entrySet()) {
			if ((entry.getValue() != null && !entry.getValue().isEmpty())) { // && (channels.isEmpty() || channels.contains(entry.getKey().a))) { TODO
				for (T coordInfo : (MonitoredList<T>) entry.getValue()) {
					updateList.addInfoToList(coordInfo, (MonitoredList<T>) entry.getValue());
				}
				updateList.sizing.add(entry.getValue().sizing);
				entities.add(entry.getKey());
				if (monitor.channelType() == ChannelType.SINGLE) {
					break;
				}
			}
		}
		return updateList;
	}

	public void sendPacketsToViewers(ILogicMonitor monitor, MonitoredList saveList, MonitoredList lastList) {
		IViewersList viewers = monitor.getViewersList();
		ArrayList<EntityPlayer> players = viewers.getViewers(true, ViewerType.INFO, ViewerType.FULL_INFO, ViewerType.TEMPORARY);
		MonitoredList<MonitoredBlockCoords> coords = Logistics.getNetworkManager().getCoordMap().get(getNetworkID());
		NBTTagCompound coordTag = !viewers.getViewers(true, ViewerType.CHANNEL).isEmpty() ? InfoHelper.writeMonitoredList(new NBTTagCompound(), coords.isEmpty(), coords.copyInfo(), SyncType.DEFAULT_SYNC) : null;
		NBTTagCompound saveTag = !viewers.getViewers(true, ViewerType.FULL_INFO, ViewerType.TEMPORARY).isEmpty() ? InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.DEFAULT_SYNC) : null;
		NBTTagCompound tag = !viewers.getViewers(true, ViewerType.INFO).isEmpty() ? InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.SPECIAL) : null;
		if ((saveTag != null && !saveTag.hasNoTags()) || (tag != null && !tag.hasNoTags()) || (coordTag != null && !coordTag.hasNoTags())) {
			// if (resendAllLists) {
			for (Entry<EntityPlayer, ArrayList<ViewerTally>> entry : ((HashMap<EntityPlayer, ArrayList<ViewerTally>>) viewers.getViewers(true).clone()).entrySet()) {
				for (ViewerTally tally : (ArrayList<ViewerTally>) entry.getValue().clone()) {
					switch (tally.type) {
					case CHANNEL:
						if (!coordTag.hasNoTags()){
							Logistics.network.sendTo(new PacketMonitoredCoords(getNetworkID(), coordTag), (EntityPlayerMP) entry.getKey());
							tally.origin.removeViewer(entry.getKey(), ViewerType.CHANNEL);
						}
						break;
					case INFO:
						if (!tag.hasNoTags() && (!saveList.changed.isEmpty() || !saveList.removed.isEmpty()))
							Logistics.network.sendTo(new PacketMonitoredList(monitor, new InfoUUID(monitor.getIdentity().hashCode(), 0), saveList.networkID, tag, SyncType.SPECIAL), (EntityPlayerMP) entry.getKey());
						break;

					case FULL_INFO:
						Logistics.network.sendTo(new PacketMonitoredList(monitor, new InfoUUID(monitor.getIdentity().hashCode(), 0), saveList.networkID, saveTag, SyncType.DEFAULT_SYNC), (EntityPlayerMP) entry.getKey());
						tally.origin.removeViewer(entry.getKey(), ViewerType.FULL_INFO);
						tally.origin.addViewer(entry.getKey(), ViewerType.INFO);
						//// THIS IS ADDED TO THE WRONG LIST. NOT THE CONNECTED DISPLAY
						break;
					case TEMPORARY:
						Logistics.network.sendTo(new PacketMonitoredList(monitor, new InfoUUID(monitor.getIdentity().hashCode(), 0), saveList.networkID, saveTag, SyncType.DEFAULT_SYNC), (EntityPlayerMP) entry.getKey());
						tally.origin.removeViewer(entry.getKey(), ViewerType.TEMPORARY);
						NBTTagList list = new NBTTagList();
						for (int i = 0; i < monitor.getMaxInfo(); i++) {
							InfoUUID id = new InfoUUID(monitor.getIdentity().hashCode(), i);
							IMonitorInfo info = Logistics.getServerManager().info.get(id);
							if (info != null) {
								NBTTagCompound nbt = InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE);
								nbt = id.writeData(nbt, SyncType.SAVE);
								list.appendTag(nbt);

							}
						}
						Logistics.getServerManager().sendPlayerPacket(entry.getKey(), list, SyncType.SAVE);
						break;
					default:
						break;
					}
				}
			}
		}
	}

	public MonitoredList<?> getListForMonitor(ILogicMonitor monitor) {
		return monitorInfo.get(monitor.getHandler()).get(monitor);
	}

	public <T extends IMonitorInfo> Map<NodeConnection, MonitoredList<?>> getTileMonitoredList(LogicMonitorHandler<T> type) {
		Map<NodeConnection, MonitoredList<?>> coordInfo = new LinkedHashMap();
		if (type instanceof ITileMonitorHandler) {
			Map<NodeConnection, MonitoredList<?>> infoList = tileConnectionInfo.getOrDefault(type, new LinkedHashMap());
			for (Entry<NodeConnection, MonitoredList<?>> entry : infoList.entrySet()) {
				MonitoredList<T> oldList = entry.getValue() == null ? MonitoredList.<T>newMonitoredList(getNetworkID()) : (MonitoredList<T>) entry.getValue();
				MonitoredList<T> list = ((ITileMonitorHandler) type).updateInfo(this, oldList, entry.getKey());
				coordInfo.put(entry.getKey(), list);
			}
		}
		return coordInfo;
	}

	public <T extends IMonitorInfo> Map<NodeConnection, MonitoredList<?>> getTileMonitoredList(LogicMonitorHandler<T> type, ArrayList<BlockCoords> coords) {
		Map<NodeConnection, MonitoredList<?>> coordInfo = new LinkedHashMap();
		if (type instanceof ITileMonitorHandler) {
			Map<NodeConnection, MonitoredList<?>> infoList = tileConnectionInfo.getOrDefault(type, new LinkedHashMap());
			for (Entry<NodeConnection, MonitoredList<?>> entry : infoList.entrySet()) {
				MonitoredList<T> oldList = entry.getValue() == null ? MonitoredList.<T>newMonitoredList(getNetworkID()) : (MonitoredList<T>) entry.getValue();
				if (coords.contains(entry.getKey().coords)) {
					MonitoredList<T> list = ((ITileMonitorHandler) type).updateInfo(this, oldList, entry.getKey());
					coordInfo.put(entry.getKey(), list);
				} else {
					coordInfo.put(entry.getKey(), oldList);
				}
			}
		}
		return coordInfo;
	}

	public <T extends IMonitorInfo> Map<Entity, MonitoredList<?>> getEntityMonitoredList(LogicMonitorHandler<T> type) {
		Map<Entity, MonitoredList<?>> coordInfo = new LinkedHashMap();
		if (type instanceof IEntityMonitorHandler) {
			Map<Entity, MonitoredList<?>> infoList = entityConnectionInfo.getOrDefault(type, new LinkedHashMap());
			for (Entry<Entity, MonitoredList<?>> entry : infoList.entrySet()) {
				MonitoredList<T> oldList = entry.getValue() == null ? MonitoredList.<T>newMonitoredList(getNetworkID()) : (MonitoredList<T>) entry.getValue();
				MonitoredList<T> list = ((IEntityMonitorHandler) type).updateInfo(this, oldList, entry.getKey());
				coordInfo.put(entry.getKey(), list);
			}
		}
		return coordInfo;
	}

	public <T extends IMonitorInfo> Map<Entity, MonitoredList<?>> getEntityMonitoredList(LogicMonitorHandler<T> type, ArrayList<Entity> coords) {
		Map<Entity, MonitoredList<?>> coordInfo = new LinkedHashMap();
		if (type instanceof IEntityMonitorHandler) {
			Map<Entity, MonitoredList<?>> infoList = entityConnectionInfo.getOrDefault(type, new LinkedHashMap());
			for (Entry<Entity, MonitoredList<?>> entry : infoList.entrySet()) {
				MonitoredList<T> oldList = entry.getValue() == null ? MonitoredList.<T>newMonitoredList(getNetworkID()) : (MonitoredList<T>) entry.getValue();
				if (coords.isEmpty() || coords.contains(entry.getKey())) {
					MonitoredList<T> list = ((IEntityMonitorHandler) type).updateInfo(this, oldList, entry.getKey());
					coordInfo.put(entry.getKey(), list);
				} else {
					coordInfo.put(entry.getKey(), oldList);
				}
			}
		}
		return coordInfo;
	}

	public abstract <T extends IMonitorInfo> void compileConnectionList(LogicMonitorHandler<T> type);
}
