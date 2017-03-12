package sonar.logistics.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.Logistics;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.displays.IInfoContainer;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IEntityMonitorHandler;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.readers.IListReader;
import sonar.logistics.api.readers.INetworkReader;
import sonar.logistics.api.readers.IdentifiedChannelsList;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.api.viewers.IViewersList;
import sonar.logistics.api.viewers.ViewerTally;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.network.PacketChannels;
import sonar.logistics.network.PacketMonitoredList;

public abstract class AbstractNetwork implements ILogisticsNetwork {

	public boolean resendAllLists = false;
	public final Map<LogicMonitorHandler, Map<NodeConnection, MonitoredList<?>>> channelConnectionInfo = new LinkedHashMap(); // block coords stored with the info gathered
	public final Map<LogicMonitorHandler, ArrayList<IListReader>> monitorInfo = new LinkedHashMap();
	public final Map<IInfoDisplay, IInfoContainer> connectedDisplays = new LinkedHashMap();
	public final ArrayList<IInfoProvider> localMonitors = new ArrayList();

	/** adds an info display to the list of display associated with this cache */
	public void addDisplay(IInfoDisplay display) {
		if (!connectedDisplays.containsKey(display)) {
			connectedDisplays.put(display, new InfoContainer(display));
		}
	}

	public void removeDisplay(IInfoDisplay display) {
		connectedDisplays.remove(display);
	}

	public <T extends IMonitorInfo> void addMonitor(IListReader<T> monitor) {
		for (LogicMonitorHandler handler : monitor.getValidHandlers()) {
			monitorInfo.putIfAbsent(handler, new ArrayList());
			if (!monitorInfo.get(handler).contains(monitor)) {
				monitorInfo.get(handler).add(monitor);
			}
			compileConnectionList(handler);
		}
	}

	public <T extends IMonitorInfo> void removeMonitor(IListReader<T> monitor) {
		for (LogicMonitorHandler handler : monitor.getValidHandlers()) {
			monitorInfo.get(handler).remove(monitor);
			compileConnectionList(handler);
		}
	}

	public void sendNormalProviderInfo(IInfoProvider provider) {
		IViewersList viewers = provider.getViewersList();
		ArrayList<EntityPlayer> players = viewers.getViewers(true, ViewerType.INFO, ViewerType.FULL_INFO, ViewerType.TEMPORARY);
		if (players.isEmpty()) {
			return;
		}
		MonitoredList<IMonitorInfo> coords = Logistics.getNetworkManager().getCoordMap().get(getNetworkID());
		NBTTagCompound coordTag = !viewers.getViewers(true, ViewerType.CHANNEL).isEmpty() ? InfoHelper.writeMonitoredList(new NBTTagCompound(), coords.isEmpty(), coords.copyInfo(), SyncType.DEFAULT_SYNC) : null;

		for (Entry<EntityPlayer, ArrayList<ViewerTally>> entry : ((HashMap<EntityPlayer, ArrayList<ViewerTally>>) viewers.getViewers(true).clone()).entrySet()) {
			for (ViewerTally tally : (ArrayList<ViewerTally>) entry.getValue().clone()) {
				switch (tally.type) {
				case CHANNEL:
					if (!coordTag.hasNoTags()) {
						Logistics.network.sendTo(new PacketChannels(getNetworkID(), coordTag), (EntityPlayerMP) entry.getKey());
						tally.origin.removeViewer(entry.getKey(), ViewerType.CHANNEL);
					}
					break;
				case FULL_INFO:
					tally.origin.removeViewer(entry.getKey(), ViewerType.FULL_INFO);
					tally.origin.addViewer(entry.getKey(), ViewerType.INFO);
					//// THIS IS ADDED TO THE WRONG LIST. NOT THE CONNECTED DISPLAY
					break;
				case TEMPORARY:
					tally.origin.removeViewer(entry.getKey(), ViewerType.TEMPORARY);
					NBTTagList list = new NBTTagList();
					for (int i = 0; i < provider.getMaxInfo(); i++) {
						InfoUUID id = new InfoUUID(provider.getIdentity().hashCode(), i);
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

	public void sendPacketsToViewers(ILogicViewable monitor, MonitoredList saveList, MonitoredList lastList, int id) {
		IViewersList viewers = monitor.getViewersList();
		ArrayList<EntityPlayer> players = viewers.getViewers(true, ViewerType.INFO, ViewerType.FULL_INFO, ViewerType.TEMPORARY);
		MonitoredList<IMonitorInfo> coords = Logistics.getNetworkManager().getCoordMap().get(getNetworkID());
		NBTTagCompound coordTag = !viewers.getViewers(true, ViewerType.CHANNEL).isEmpty() ? InfoHelper.writeMonitoredList(new NBTTagCompound(), coords.isEmpty(), coords.copyInfo(), SyncType.DEFAULT_SYNC) : null;
		NBTTagCompound saveTag = !viewers.getViewers(true, ViewerType.FULL_INFO, ViewerType.TEMPORARY).isEmpty() ? InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.DEFAULT_SYNC) : null;
		NBTTagCompound tag = !viewers.getViewers(true, ViewerType.INFO).isEmpty() ? InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.SPECIAL) : null;
		if ((saveTag != null && !saveTag.hasNoTags()) || (tag != null && !tag.hasNoTags()) || (coordTag != null && !coordTag.hasNoTags())) {
			// if (resendAllLists) {
			for (Entry<EntityPlayer, ArrayList<ViewerTally>> entry : ((HashMap<EntityPlayer, ArrayList<ViewerTally>>) viewers.getViewers(true).clone()).entrySet()) {
				for (ViewerTally tally : (ArrayList<ViewerTally>) entry.getValue().clone()) {
					switch (tally.type) {
					case CHANNEL:
						if (!coordTag.hasNoTags()) {
							Logistics.network.sendTo(new PacketChannels(getNetworkID(), coordTag), (EntityPlayerMP) entry.getKey());
							tally.origin.removeViewer(entry.getKey(), ViewerType.CHANNEL);
						}
						break;
					case INFO:
						if (!tag.hasNoTags() && (!saveList.changed.isEmpty() || !saveList.removed.isEmpty()))
							Logistics.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), new InfoUUID(monitor.getIdentity().hashCode(), id), saveList.networkID, tag, SyncType.SPECIAL), (EntityPlayerMP) entry.getKey());
						break;

					case FULL_INFO:
						Logistics.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), new InfoUUID(monitor.getIdentity().hashCode(), id), saveList.networkID, saveTag, SyncType.DEFAULT_SYNC), (EntityPlayerMP) entry.getKey());
						tally.origin.removeViewer(entry.getKey(), ViewerType.FULL_INFO);
						tally.origin.addViewer(entry.getKey(), ViewerType.INFO);
						break;
					case TEMPORARY:
						Logistics.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), new InfoUUID(monitor.getIdentity().hashCode(), id), saveList.networkID, saveTag, SyncType.DEFAULT_SYNC), (EntityPlayerMP) entry.getKey());
						tally.origin.removeViewer(entry.getKey(), ViewerType.TEMPORARY);
						if (monitor instanceof INetworkReader) {
							INetworkReader reader = (INetworkReader) monitor;
							NBTTagList list = new NBTTagList();
							for (int i = 0; i < reader.getMaxInfo(); i++) {
								InfoUUID infoID = new InfoUUID(reader.getIdentity().hashCode(), i);
								IMonitorInfo info = Logistics.getServerManager().info.get(infoID);
								if (info != null) {
									NBTTagCompound nbt = InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE);
									nbt = infoID.writeData(nbt, SyncType.SAVE);
									list.appendTag(nbt);
								}
							}
							Logistics.getServerManager().sendPlayerPacket(entry.getKey(), list, SyncType.SAVE);
						}
						break;
					default:
						break;
					}
				}
			}
		}
	}

	public <T extends IMonitorInfo> Map<NodeConnection, MonitoredList<?>> getChannels(LogicMonitorHandler<T> type, IdentifiedChannelsList channels) {
		Map<NodeConnection, MonitoredList<?>> coordInfo = new LinkedHashMap();
		Map<NodeConnection, MonitoredList<?>> infoList = channelConnectionInfo.getOrDefault(type, new LinkedHashMap());
		for (Entry<NodeConnection, MonitoredList<?>> entry : infoList.entrySet()) {
			MonitoredList<T> oldList = entry.getValue() == null ? MonitoredList.<T>newMonitoredList(getNetworkID()) : (MonitoredList<T>) entry.getValue();
			MonitoredList<T> list = null;
			if (entry.getKey() instanceof BlockConnection) {
				BlockConnection connection = (BlockConnection) entry.getKey();
				if (channels == null || channels.isCoordsMonitored(connection.coords)) {
					list = ((ITileMonitorHandler) type).updateInfo(this, oldList, connection);
				}
			} else if (entry.getKey() instanceof EntityConnection) {
				EntityConnection connection = (EntityConnection) entry.getKey();
				if (channels == null || channels.isEntityMonitored(connection.entity.getPersistentID())) {
					list = ((IEntityMonitorHandler) type).updateInfo(this, oldList, connection);
				}
			}
			coordInfo.put(entry.getKey(), list == null ? oldList : list);
		}
		return coordInfo;
	}

	/* public <T extends IMonitorInfo> Map<BlockConnection, MonitoredList<?>> getTileMonitoredList(LogicMonitorHandler<T> type, IdentifiedChannelsList channels) { Map<BlockConnection, MonitoredList<?>> coordInfo = new LinkedHashMap(); if (type instanceof ITileMonitorHandler) { Map<BlockConnection, MonitoredList<?>> infoList = tileConnectionInfo.getOrDefault(type, new LinkedHashMap()); for (Entry<BlockConnection, MonitoredList<?>> entry : infoList.entrySet()) { MonitoredList<T> oldList = entry.getValue() == null ? MonitoredList.<T>newMonitoredList(getNetworkID()) : (MonitoredList<T>) entry.getValue(); if (channels == null || channels.isCoordsMonitored(entry.getKey().coords)) { MonitoredList<T> list = ((ITileMonitorHandler) type).updateInfo(this, oldList, entry.getKey()); coordInfo.put(entry.getKey(), list); } else { coordInfo.put(entry.getKey(), oldList); } } } return coordInfo; } public <T extends IMonitorInfo> Map<EntityConnection, MonitoredList<?>> getEntityMonitoredList(LogicMonitorHandler<T> type, IdentifiedChannelsList channels) { Map<EntityConnection, MonitoredList<?>> coordInfo = new LinkedHashMap(); if (type instanceof IEntityMonitorHandler) { Map<EntityConnection, MonitoredList<?>> infoList = entityConnectionInfo.getOrDefault(type, new LinkedHashMap()); for (Entry<EntityConnection, MonitoredList<?>> entry : infoList.entrySet()) { MonitoredList<T> oldList = entry.getValue() == null ? MonitoredList.<T>newMonitoredList(getNetworkID()) : (MonitoredList<T>) entry.getValue(); if (channels == null || channels.isEntityMonitored(entry.getKey().entity.getPersistentID())) { MonitoredList<T> list = ((IEntityMonitorHandler) type).updateInfo(this, oldList, entry.getKey()); coordInfo.put(entry.getKey(), list); } else { coordInfo.put(entry.getKey(), oldList); } } } return coordInfo; } */
	public abstract <T extends IMonitorInfo> void compileConnectionList(LogicMonitorHandler<T> type);
}
