package sonar.logistics.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import akka.routing.Listeners;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.logistics.PL2;
import sonar.logistics.PL2Config;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.connecting.INetworkListener;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.info.IEntityMonitorHandler;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.nodes.TransferType;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.readers.IListReader;
import sonar.logistics.api.readers.INetworkReader;
import sonar.logistics.api.readers.IdentifiedChannelsList;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.common.multiparts.DataEmitterPart;
import sonar.logistics.connections.monitoring.FluidMonitorHandler;
import sonar.logistics.connections.monitoring.InfoMonitorHandler;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.FluidHelper;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.ItemHelper;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.network.PacketChannels;
import sonar.logistics.network.PacketMonitoredList;

public class LogisticsNetwork implements ILogisticsNetwork {

	public final Map<LogicMonitorHandler, Map<NodeConnection, MonitoredList<?>>> channelConnectionInfo = new LinkedHashMap();
	public final Map<LogicMonitorHandler, ArrayList<IListReader>> monitorInfo = new LinkedHashMap();
	private ArrayList<CacheHandler> changedCaches = (ArrayList<CacheHandler>) CacheHandler.handlers.clone();
	private HashMap<CacheHandler, ArrayList> caches = LogisticsHelper.getCachesMap();
	public Queue<INetworkListener> toAdd = new ConcurrentLinkedQueue<INetworkListener>();
	public Queue<INetworkListener> toRemove = new ConcurrentLinkedQueue<INetworkListener>();
	private ArrayList<NodeConnection> localChannels = Lists.newArrayList(), globalChannels = Lists.newArrayList(), allChannels = Lists.newArrayList();
	public final ArrayList<IInfoProvider> localMonitors = new ArrayList();
	public int updateTicks = 0;

	public ListenerList<ILogisticsNetwork> subNetworks = new ListenerList(this, 2);

	public int networkID;
	public boolean isValid = true;

	public LogisticsNetwork(int networkID) {
		this.networkID = networkID;
	}

	public boolean shouldTick() {
		if (this.updateTicks < PL2Config.updateRate) {
			updateTicks++;
			return false;
		}
		updateTicks = 0;
		return true;
	}

	public void onNetworkTick() {
		addConnections();
		removeConnections();
		if (shouldTick()) {
			updateCaches();
			updateMonitorListeners();
			updateTransferNetwork();
		}
	}

	public void updateMonitorListeners() {
		for (Entry<LogicMonitorHandler, ArrayList<IListReader>> monitorMap : monitorInfo.entrySet()) {
			if (!monitorMap.getValue().isEmpty()) {
				Map<NodeConnection, MonitoredList<?>> newChannels;
				IdentifiedChannelsList list = null;
				if (monitorMap.getKey().id() == InfoMonitorHandler.id) {
					list = new IdentifiedChannelsList(-1, ChannelType.NETWORK_SINGLE, networkID);
					for (IListReader monitor : monitorMap.getValue()) {
						if (monitor instanceof INetworkReader && !monitor.getListenerList().getListeners(ListenerType.FULL_INFO, ListenerType.TEMPORARY).isEmpty()) {
							IdentifiedChannelsList channels = ((INetworkReader) monitor).getChannels();
							list.addAll(channels.getCoords());
							list.addAllUUID(channels.getUUIDs());
						}
					}
				}
				newChannels = getChannels(monitorMap.getKey(), list);
				channelConnectionInfo.replace(monitorMap.getKey(), newChannels);// the connectionInfo is saved.
				updateAndSendLists(monitorMap, newChannels);
			}
		}
		for (IInfoProvider provider : localMonitors) {
			sendNormalProviderInfo(provider);
		}
	}

	public void updateCoordsList() {
		MonitoredList<IMonitorInfo> list = MonitoredList.<IMonitorInfo>newMonitoredList(getNetworkID());
		monitorInfo.entrySet().forEach(handlers -> compileConnectionList(handlers.getKey()));
		allChannels.forEach(CHANNEL -> list.add(CHANNEL.getChannel()));
		PL2.getNetworkManager().getCoordMap().put(networkID, list);
	}

	public <T extends IMonitorInfo> Map<NodeConnection, MonitoredList<?>> getChannels(LogicMonitorHandler<T> type, IdentifiedChannelsList channels) {
		Map<NodeConnection, MonitoredList<?>> coordInfo = new LinkedHashMap();
		Map<NodeConnection, MonitoredList<?>> infoList = channelConnectionInfo.getOrDefault(type, new LinkedHashMap());
		for (Entry<NodeConnection, MonitoredList<?>> entry : infoList.entrySet()) {
			if (validateTile(entry.getKey().source)) {
				MonitoredList<T> oldList = entry.getValue() == null ? MonitoredList.<T>newMonitoredList(getNetworkID()) : (MonitoredList<T>) entry.getValue();
				MonitoredList<T> list = null;
				if (entry.getKey() instanceof BlockConnection) {
					BlockConnection connection = (BlockConnection) entry.getKey();
					if (channels == null || channels.isCoordsMonitored(connection.coords)) {
						list = ((ITileMonitorHandler) type).updateInfo(this, oldList, connection);
					}
				} else if (entry.getKey() instanceof EntityConnection && type instanceof IEntityMonitorHandler) {
					EntityConnection connection = (EntityConnection) entry.getKey();
					if (channels == null || channels.isEntityMonitored(connection.entity.getPersistentID())) {
						list = ((IEntityMonitorHandler) type).updateInfo(this, oldList, connection);
					}
				}
				coordInfo.put(entry.getKey(), list == null ? oldList : list);
			}
		}
		return coordInfo;
	}

	public void updateAndSendLists(Entry<LogicMonitorHandler, ArrayList<IListReader>> monitorMap, Map<NodeConnection, MonitoredList<?>> newChannels) {
		for (IListReader monitor : monitorMap.getValue()) {
			if (monitor != null && validateTile(monitor)) {
				ArrayList<NodeConnection> usedChannels = new ArrayList();
				int id = monitor instanceof IDataEmitter ? (monitorMap.getKey().id().equals(FluidMonitorHandler.id) ? DataEmitterPart.STATIC_FLUID_ID : DataEmitterPart.STATIC_ITEM_ID) : 0;
				InfoUUID infoID = new InfoUUID(monitor.getIdentity(), id);
				MonitoredList lastList = PL2.getServerManager().monitoredLists.getOrDefault(infoID, MonitoredList.newMonitoredList(getNetworkID()));

				MonitoredList updateList = monitor.getUpdatedList(id, newChannels, usedChannels).updateList(lastList);
				if (monitor instanceof INetworkReader) {
					((INetworkReader) monitor).setMonitoredInfo(updateList, usedChannels, id);
				}
				sendPacketsToViewers(monitor, updateList.copyInfo(), lastList, id);
				PL2.getServerManager().monitoredLists.put(infoID, updateList);
			}
		}
	}

	public void updateTransferNetwork() {
		List<ITransferFilteredTile> transferNodes = getConnections(CacheHandler.TRANSFER_NODES, CacheType.ALL);
		if (transferNodes.isEmpty()) {
			return;
		}
		for (ITransferFilteredTile tile : transferNodes) {
			BlockConnection connected = tile.getConnected();
			NodeTransferMode mode = tile.getTransferMode();
			if (connected == null || mode.isPassive()) {
				continue;
			}
			boolean items = tile.isTransferEnabled(TransferType.ITEMS);
			boolean fluids = tile.isTransferEnabled(TransferType.FLUID);
			if (items || fluids) {
				for (NodeConnection connect : allChannels) {
					if (connect != null && connect instanceof BlockConnection && connect.source != connected.source && tile.getChannels().isMonitored(connect)) {
						if (items)
							ItemHelper.transferItems(mode, connected, (BlockConnection) connect);
						if (fluids)
							FluidHelper.transferFluids(mode, connected, (BlockConnection) connect);
					}
				}
				// TODO entities
			}

		}
	}

	public void sendNormalProviderInfo(IInfoProvider monitor) {
		sendPacketsToViewers(monitor, null, null, 0);
	}

	public void sendPacketsToViewers(ILogicViewable monitor, MonitoredList saveList, MonitoredList lastList, int id) {
		InfoUUID uuid = new InfoUUID(monitor.getIdentity(), id);
		ListenerList<PlayerListener> list = monitor.getListenerList();
		for (ListenerType type : ListenerType.ALL) {
			ArrayList<PlayerListener> listeners = list.getListeners(type);
			if (listeners.isEmpty()) {
				continue;
			}
			switch (type) {
			case CHANNEL:
				MonitoredList<IMonitorInfo> coords = PL2.getNetworkManager().getCoordMap().get(getNetworkID());
				NBTTagCompound channelTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), coords.isEmpty(), coords.copyInfo(), SyncType.DEFAULT_SYNC);
				if (channelTag.hasNoTags())
					continue;
				listeners.forEach(listener -> {
					PL2.network.sendTo(new PacketChannels(getNetworkID(), channelTag), listener.player);
					list.removeListener(listener, ListenerType.CHANNEL);
				});
				break;
			case FULL_INFO:
				NBTTagCompound saveTag = saveList != null ? InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.DEFAULT_SYNC) : null;
				if (saveTag.hasNoTags())
					continue;
				listeners.forEach(listener -> {
					if (saveList != null)
						PL2.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), uuid, networkID, saveTag, SyncType.DEFAULT_SYNC), listener.player);
					list.removeListener(listener, ListenerType.FULL_INFO);
					list.addListener(listener, ListenerType.INFO);
				});
				break;
			case INFO:
				if (saveList == null) {
					continue;
				}
				NBTTagCompound tag = InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.SPECIAL);
				if (tag.hasNoTags() || (saveList.changed.isEmpty() && saveList.removed.isEmpty())) {
					continue;
				}
				listeners.forEach(listener -> PL2.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), uuid, networkID, tag, SyncType.SPECIAL), listener.player));
				break;
			case TEMPORARY:
				saveTag = saveList != null ? InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.DEFAULT_SYNC) : null;
				NBTTagList tagList = new NBTTagList();
				if (monitor instanceof INetworkReader) {
					INetworkReader reader = (INetworkReader) monitor;
					for (int i = 0; i < reader.getMaxInfo(); i++) {
						InfoUUID infoID = new InfoUUID(reader.getIdentity(), i);
						IMonitorInfo info = PL2.getServerManager().info.get(infoID);
						if (info != null) {
							NBTTagCompound nbt = InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE);
							nbt = infoID.writeData(nbt, SyncType.SAVE);
							tagList.appendTag(nbt);
						}
					}
				}
				listeners.forEach(listener -> {
					if (saveList != null)
						PL2.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), uuid, saveList.networkID, saveTag, SyncType.DEFAULT_SYNC), listener.player);

					list.removeListener(listener, ListenerType.TEMPORARY);
					PL2.getServerManager().sendPlayerPacket(listener, tagList, SyncType.SAVE);
				});

				break;
			default:
				break;

			}
		}
	}

	public <T> ArrayList<T> getConnections(CacheHandler<T> handler, CacheType cacheType) {
		ArrayList<T> tiles = cacheType.isLocal() ? (ArrayList<T>) caches.getOrDefault(handler, new ArrayList()).clone() : new ArrayList();
		if (cacheType.isGlobal()) {
			ArrayList<ILogisticsNetwork> connected = subNetworks.getListeners(ILogisticsNetwork.CONNECTED_NETWORK);
			connected.forEach(network -> ListHelper.addWithCheck(tiles, network.getConnections(handler, CacheType.LOCAL)));
		}
		return tiles;
	}

	@Override
	public void addConnection(INetworkListener tile) {
		toAdd.add(tile);
		// toRemove.remove(tile);
	}

	@Override
	public void removeConnection(INetworkListener tile) {
		toRemove.add(tile);
		// toAdd.remove(tile);
	}

	@Override
	public void onNetworkRemoved() {
		isValid = false;
		ArrayList<ILogisticsNetwork> watching = subNetworks.getListeners(ILogisticsNetwork.WATCHING_NETWORK);
		watching.forEach(network -> network.getListenerList().removeListener(this, ILogisticsNetwork.CONNECTED_NETWORK));
		subNetworks.invalidateList();
		getConnections(CacheHandler.TILE, CacheType.LOCAL).forEach(t -> t.onNetworkDisconnect(this));
		
		//TODO - Check if these need to be cleared, the network is removed so these shouldn't need to be.
		//caches.clear();
		//toAdd.clear();
		//toRemove.clear();
	}

	public void markCacheDirty(CacheHandler cache) {
		if (!changedCaches.contains(cache)) {
			changedCaches.add(cache);
		}
	}

	public void addConnections() {
		if (toAdd.isEmpty())
			return;
		Iterator<INetworkListener> iterator = toAdd.iterator();
		while (iterator.hasNext()) {
			INetworkListener tile = iterator.next();
			CacheHandler.getValidCaches(tile).forEach(cache -> {
				if (!caches.get(cache).contains(tile) && caches.get(cache).add(tile)) {
					markCacheDirty(cache);
					cache.onConnectionAdded(this, tile);
				}
			});
			iterator.remove();
		}
	}

	public void removeConnections() {
		if (toRemove.isEmpty())
			return;
		Iterator<INetworkListener> iterator = toRemove.iterator();
		while (iterator.hasNext()) {
			INetworkListener tile = iterator.next();
			CacheHandler.getValidCaches(tile).forEach(cache -> {
				if (caches.get(cache).remove(tile)) {
					markCacheDirty(cache);
					cache.onConnectionRemoved(this, tile);
				}
			});
			iterator.remove();
		}
	}

	public void updateChannels() {
		updateLocalChannels();
		updateGlobalChannels();
	}

	public void updateLocalChannels() {
		ArrayList<NodeConnection> channels = new ArrayList();
		LogisticsHelper.sortNodeConnections(channels, getConnections(CacheHandler.NODES, CacheType.LOCAL));
		this.localChannels = (ArrayList<NodeConnection>) channels.clone();
	}

	public void updateGlobalChannels() {
		ArrayList<NodeConnection> channels = new ArrayList();
		LogisticsHelper.sortNodeConnections(channels, getConnections(CacheHandler.NODES, CacheType.GLOBAL));
		this.globalChannels = (ArrayList<NodeConnection>) channels.clone();

		ArrayList<NodeConnection> all = new ArrayList();
		ListHelper.addWithCheck(all, globalChannels);
		ListHelper.addWithCheck(all, localChannels);
		NodeConnection.sortConnections(all);
		this.allChannels = all;
	}

	@Override
	public ArrayList<NodeConnection> getChannels(CacheType cacheType) {
		switch (cacheType) {
		case GLOBAL:
			return globalChannels;
		case LOCAL:
			return localChannels;
		default:
			return allChannels;
		}
	}

	@Override
	public void onConnectionChanged(INetworkListener tile) {
		if (validateTile(tile)) {
			CacheHandler.getValidCaches(tile).forEach(cache -> {
				if (!changedCaches.contains(cache)) {
					changedCaches.add(cache);
				}
			});
		}
	}

	public void updateSubNetworks() {
		getConnections(CacheHandler.RECEIVERS, CacheType.LOCAL).forEach(r -> {
			if (validateTile(r)) {
				LogisticsHelper.addConnectedNetworks(this, r);
			}
		});
	}

	public boolean validateTile(INetworkListener listener) {
		if (!listener.isValid()){// || listener.getNetworkID() != this.networkID) {
			removeConnection(listener);
			return false;
		}
		return true;
	}

	public void updateCaches() {
		changedCaches.forEach(handler -> handler.update(this, caches.get(handler)));
		changedCaches.clear();
	}

	@Override
	public int getNetworkID() {
		return networkID;
	}

	@Override
	public boolean isFakeNetwork() {
		return !isValid();
	}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {
		localMonitors.add(monitor);
	}

	@Override
	public void removeLocalInfoProvider(IInfoProvider monitor) {
		localMonitors.remove(monitor);
	}

	@Override
	public IInfoProvider getLocalInfoProvider() {
		if (!localMonitors.isEmpty()) {
			return localMonitors.get(0);
		}
		return null;
	}

	@Override
	public ArrayList<IInfoProvider> getLocalInfoProviders() {
		return localMonitors;
	}

	public <T extends IMonitorInfo> void compileConnectionList(LogicMonitorHandler<T> type) {
		HashMap<NodeConnection, MonitoredList<?>> compiledList = new HashMap();
		for (NodeConnection connection : allChannels) {
			if (connection.source.isValid() && !compiledList.containsKey(connection)) {
				Map<NodeConnection, MonitoredList<?>> map = channelConnectionInfo.computeIfAbsent(type, h -> new HashMap());
				MonitoredList<?> list = map.computeIfAbsent(connection, m -> MonitoredList.newMonitoredList(getNetworkID()));
				compiledList.put(connection, list);
			}
		}
		channelConnectionInfo.put(type, compiledList);
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Override
	public ListenerList<ILogisticsNetwork> getListenerList() {
		return subNetworks;
	}

	@Override
	public void onListenerAdded(ListenerTally<ILogisticsNetwork> tally) {
		tally.listener.getListenerList().addListener(tally.listener, ILogisticsNetwork.CONNECTED_NETWORK);
	}

	@Override
	public void onListenerRemoved(ListenerTally<ILogisticsNetwork> tally) {
		tally.listener.getListenerList().removeListener(tally.listener, ILogisticsNetwork.CONNECTED_NETWORK);
	}

}
