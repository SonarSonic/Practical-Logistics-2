package sonar.logistics.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.ListHelper;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.Logistics;
import sonar.logistics.LogisticsConfig;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.*;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.connecting.IRefreshCache;
import sonar.logistics.api.connecting.RefreshType;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.IEntityNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.nodes.TransferType;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.readers.IListReader;
import sonar.logistics.api.readers.INetworkReader;
import sonar.logistics.api.readers.IdentifiedChannelsList;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.common.multiparts.DataCablePart;
import sonar.logistics.common.multiparts.DataEmitterPart;
import sonar.logistics.common.multiparts.InfoReaderPart;
import sonar.logistics.connections.monitoring.FluidMonitorHandler;
import sonar.logistics.connections.monitoring.InfoMonitorHandler;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.FluidHelper;
import sonar.logistics.helpers.ItemHelper;

public class DefaultNetwork extends AbstractNetwork implements IRefreshCache {
	
	private ArrayList<Class<?>> cacheTypes = Lists.newArrayList(IDataCable.class, ILogicTile.class, INetworkReader.class, IDataReceiver.class, IDataEmitter.class, ITransferFilteredTile.class, IConnectionNode.class, IEntityNode.class);
	public int networkID = -1;
	private HashMap<Class<?>, ArrayList<IWorldPosition>> connections = getFreshMap();
	private ArrayList<NodeConnection> channelCache = Lists.newArrayList(), networkedChannelCache = Lists.newArrayList();
	private ArrayList<Integer> connectedNetworks = new ArrayList();
	private RefreshType lastRefresh = RefreshType.NONE;
	private RefreshType toRefresh = RefreshType.FULL;
	public int updateTicks = 0;

	public DefaultNetwork(int networkID) {
		this.networkID = networkID;
	}

	public HashMap<Class<?>, ArrayList<IWorldPosition>> getFreshMap() {
		HashMap<Class<?>, ArrayList<IWorldPosition>> connections = new HashMap();
		cacheTypes.forEach(classType -> connections.put(classType, new ArrayList()));
		return connections;
	}

	public ArrayList<Class<?>> getValidClasses(IWorldPosition tile) {
		ArrayList<Class<?>> valid = new ArrayList();
		for (Class<?> classType : cacheTypes) {
			if (classType.isInstance(tile)) {
				valid.add(classType);
			}
		}
		return valid;
	}

	public <T extends IWorldPosition> ArrayList<T> getConnections(Class<T> classType, boolean includeChannels) {
		ArrayList<T> list = (ArrayList<T>) connections.getOrDefault(classType, (ArrayList<IWorldPosition>) new ArrayList<T>());
		if (includeChannels) {
			ArrayList<Integer> networks = getAllConnectedNetworks();
			networks.forEach(id -> {
				INetworkCache network = Logistics.getNetworkManager().getNetwork(id);
				ListHelper.addWithCheck(list, network.getConnections(classType, false));
			});
		}
		return list;
	}

	@Override
	public ArrayList<NodeConnection> getConnectedChannels(boolean includeChannels) {
		return includeChannels ? networkedChannelCache : channelCache;
	}

	@Override
	public void refreshCache(int networkID, RefreshType refresh) {
		this.networkID = networkID;
		if (refresh.shouldRefreshCables()) {
			refreshCables();
		}
		if (refresh.shouldRefreshConnections()) {
			buildLocalConnections();
			buildNetworkConnections();
		}

		if (Logistics.getNetworkManager().updateEmitters) {
			for (IDataReceiver receiver : getConnections(IDataReceiver.class, true)) {
				receiver.refreshConnectedNetworks();
			}
		}
		if (refresh.shouldRefreshNetworks()) {
			getConnectedNetworks();
			buildLocalConnections();
			buildNetworkConnections();
			updateCoordsList();
		}
		if (refresh.shouldAlertWatchingNetworks()) {
			alertWatchingNetworks();
		}
		lastRefresh = toRefresh;
		toRefresh = RefreshType.NONE;
	}

	public void refreshCables() {
		localMonitors.clear();
		HashMap<Class<?>, ArrayList<IWorldPosition>> newConnections = getFreshMap();
		ArrayList<IDataCable> cables = Logistics.getCableManager().getConnections(networkID);
		Iterator<IDataCable> iterator = cables.iterator();
		while (iterator.hasNext()) {
			BlockCoords coord = iterator.next().getCoords();
			DataCablePart cablePart = (DataCablePart) LogisticsAPI.getCableHelper().getCableFromCoords(coord);
			if (cablePart == null) {
				continue;
			}
			cablePart.configureConnections(this);
			ArrayList<ILogicTile> tiles = CableHelper.getConnectedTiles(cablePart);
			tiles.forEach(tile -> getValidClasses(tile).forEach(classType -> newConnections.get(classType).add(tile)));
			if (!tiles.isEmpty())
				newConnections.get(IDataCable.class).add(cablePart);
		}
		this.connections = newConnections;
	}

	public void alertWatchingNetworks() {
		ArrayList<Integer> networks = getWatchingNetworks();
		networks.forEach(id -> Logistics.getNetworkManager().markNetworkDirty(id, RefreshType.ALERT));
	}

	public ArrayList<Integer> getConnectedNetworks() {
		ArrayList<Integer> networks = new ArrayList();
		getConnections(IDataReceiver.class, true).forEach(receiver -> IDataReceiver.addConnectedNetworks(receiver, networks));
		return this.connectedNetworks = networks;
	}

	public ArrayList<Integer> getWatchingNetworks() {
		ArrayList<Integer> networks = new ArrayList();
		getConnections(IDataEmitter.class, true).forEach(emitter -> IDataEmitter.addConnectedNetworks(emitter, networkID, networks));
		return networks;
	}

	public ArrayList<Integer> getAllConnectedNetworks() {
		ArrayList<Integer> networks = getConnectedNetworks(new ArrayList());
		ArrayList<Integer> fullNetworks = (ArrayList<Integer>) networks.clone();
		networks.iterator().forEachRemaining(id -> Logistics.getNetworkManager().getNetwork(id).getConnectedNetworks(fullNetworks));
		return fullNetworks;
	}

	/** this doubles checks all cables for connections as well as finding and local monitors */
	public void buildLocalConnections() {
		ArrayList<NodeConnection> channels = new ArrayList<NodeConnection>(); // array list of pairs so priority is respected

		getConnections(IConnectionNode.class, false).forEach(NODE -> IConnectionNode.addConnections(NODE, channels)); // add node connections
		getConnections(IEntityNode.class, false).forEach(ENTITY_NODE -> IEntityNode.addEntities(ENTITY_NODE, channels)); // add entity connections

		NodeConnection.sortConnections(channels);
		this.channelCache = (ArrayList<NodeConnection>) channels.clone();
	}

	public void buildNetworkConnections() {
		ArrayList<NodeConnection> map = (ArrayList<NodeConnection>) channelCache.clone(); // array list of pairs so priority is respected

		ArrayList<Integer> networks = getAllConnectedNetworks();
		for (Integer id : networks) {
			INetworkCache network = Logistics.getNetworkManager().getNetwork(id);
			ListHelper.addWithCheck(map, (List<NodeConnection>) network.getConnectedChannels(false).clone());
			ListHelper.addWithCheck(localMonitors, network.getLocalInfoProviders());
		}

		NodeConnection.sortConnections(map);
		this.networkedChannelCache = (ArrayList<NodeConnection>) map.clone();
	}

	public void updateCoordsList() {
		monitorInfo.entrySet().forEach(handlers -> compileConnectionList(handlers.getKey()));
		MonitoredList<IMonitorInfo> list = MonitoredList.<IMonitorInfo>newMonitoredList(getNetworkID());
		networkedChannelCache.forEach(CHANNEL -> list.add(CHANNEL.getChannel()));
		Logistics.getNetworkManager().getCoordMap().put(networkID, list);
	}

	public <T extends IWorldPosition> T getFirstConnection(Class<T> type) {
		ArrayList<T> coords = getConnections(type, true);
		return coords.isEmpty() ? null : coords.get(0);
	}

	@Override
	public int getNetworkID() {
		return networkID;
	}

	@Override
	public ArrayList<Integer> getConnectedNetworks(ArrayList<Integer> networks) {
		ListHelper.addWithCheck(networks, connectedNetworks);
		return networks;
	}

	@Override
	public void updateNetwork(int networkID) {
		updateTransferNetwork();
		if (this.updateTicks < LogisticsConfig.updateRate) {
			updateTicks++;
			return;
		}
		updateTicks = 0;

		resendAllLists = networkID != this.getNetworkID();
		if (resendAllLists || Logistics.getNetworkManager().updateEmitters) {
			refreshCache(networkID, RefreshType.FULL);
		} else if (toRefresh != RefreshType.NONE) {
			refreshCache(networkID, toRefresh);
		} else {
			lastRefresh = RefreshType.NONE;
		}
		toRefresh = RefreshType.NONE;

		for (Entry<LogicMonitorHandler, ArrayList<IListReader>> monitorMap : monitorInfo.entrySet()) {
			Map<NodeConnection, MonitoredList<?>> newChannels;
			if (!monitorMap.getValue().isEmpty()) {
				IdentifiedChannelsList list = null;
				if (monitorMap.getKey().id() == InfoMonitorHandler.id) {
					list = new IdentifiedChannelsList(null, ChannelType.NETWORK_SINGLE, networkID);
					for (IListReader monitor : monitorMap.getValue()) {
						if (monitor instanceof INetworkReader && !monitor.getViewersList().getViewers(true, ViewerType.FULL_INFO, ViewerType.TEMPORARY).isEmpty()) {
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
		resendAllLists = false;
	}

	public void updateAndSendLists(Entry<LogicMonitorHandler, ArrayList<IListReader>> monitorMap, Map<NodeConnection, MonitoredList<?>> newChannels) {
		for (IListReader monitor : monitorMap.getValue()) {
			if (monitor != null) {
				ArrayList<NodeConnection> usedChannels = new ArrayList();
				int id = monitor instanceof IDataEmitter ? (monitorMap.getKey().id().equals(FluidMonitorHandler.id) ? DataEmitterPart.STATIC_FLUID_ID : DataEmitterPart.STATIC_ITEM_ID) : 0;
				InfoUUID infoID = new InfoUUID(monitor.getIdentity().hashCode(), id);
				MonitoredList lastList = Logistics.getServerManager().monitoredLists.getOrDefault(infoID, MonitoredList.newMonitoredList(getNetworkID()));

				MonitoredList updateList = monitor.getUpdatedList(id, newChannels, usedChannels).updateList(lastList);
				if (monitor instanceof INetworkReader) {
					((INetworkReader) monitor).setMonitoredInfo(updateList, usedChannels, id);// TODO only one channel atm!
				}
				sendPacketsToViewers(monitor, updateList.copyInfo(), lastList, id);
				Logistics.getServerManager().monitoredLists.put(infoID, updateList);
			}
		}
	}

	public <T extends IMonitorInfo> void compileConnectionList(LogicMonitorHandler<T> type) {
		HashMap<NodeConnection, MonitoredList<?>> compiledList = new HashMap();
		for (NodeConnection pair : networkedChannelCache) {
			if (!compiledList.containsKey(pair)) {
				MonitoredList<?> list = (MonitoredList<?>) channelConnectionInfo.getOrDefault(type, new HashMap()).getOrDefault(pair, MonitoredList.<T>newMonitoredList(getNetworkID()));
				compiledList.put(pair, list);
			}
		}
		channelConnectionInfo.put(type, compiledList);
	}

	public void updateTransferNetwork() {
		ArrayList<ITransferFilteredTile> transferNodes = this.getConnections(ITransferFilteredTile.class, true);
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
				for (NodeConnection connect : networkedChannelCache) {
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

	@Override
	public boolean isFakeNetwork() {
		return false;
	}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {
		localMonitors.add(monitor);
	}

	@Override
	public IInfoProvider getLocalInfoProvider() {
		if (!localMonitors.isEmpty()) {
			return localMonitors.get(0);
		}
		return null;
	}

	@Override
	public void markDirty(RefreshType type) {
		if (type.ordinal() < toRefresh.ordinal()) {
			toRefresh = type;
		}
	}

	@Override
	public ArrayList<IInfoProvider> getLocalInfoProviders() {
		return localMonitors;
	}

	@Override
	public RefreshType getLastRefresh() {
		return lastRefresh;
	}

}