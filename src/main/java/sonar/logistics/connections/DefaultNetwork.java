package sonar.logistics.connections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.IRemovable;
import sonar.core.utils.IWorldPosition;
import sonar.core.utils.Pair;
import sonar.core.utils.SimpleProfiler;
import sonar.logistics.Logistics;
import sonar.logistics.LogisticsConfig;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.connecting.IRefreshCache;
import sonar.logistics.api.connecting.RefreshType;
import sonar.logistics.api.info.IEntityMonitorHandler;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.IEntityNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.ILogicMonitor;
import sonar.logistics.api.readers.IdentifiedCoordsList;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.common.multiparts.DataCablePart;
import sonar.logistics.connections.monitoring.InfoMonitorHandler;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.info.types.LogicInfo;

public class DefaultNetwork extends AbstractNetwork implements IRefreshCache {

	public int networkID = -1;
	private ArrayList<Class<?>> cacheTypes = Lists.newArrayList(IDataCable.class, ILogicTile.class, ILogicMonitor.class, IDataReceiver.class, IDataEmitter.class);
	private HashMap<Class<?>, ArrayList<IWorldPosition>> connections = getFreshMap();
	private ArrayList<NodeConnection> blockTileCache = Lists.newArrayList();
	private ArrayList<NodeConnection> networkedTileCache = Lists.newArrayList();
	private ArrayList<Entity> blockEntityCache = Lists.newArrayList();
	private ArrayList<Entity> networkedEntityCache = Lists.newArrayList();
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

	@Override
	@Deprecated
	public NodeConnection getExternalBlock(boolean includeChannels) {
		Iterator<IDataCable> connections = getConnections(IDataCable.class, includeChannels).iterator();
		while (connections.hasNext()) {
			IDataCable part = connections.next();
			if (part instanceof DataCablePart) {
				DataCablePart cable = (DataCablePart) part;
				ArrayList<NodeConnection> map = Lists.newArrayList();
				cable.getContainer().getParts().forEach(multipart -> {
					if (multipart instanceof IConnectionNode) {
						((IConnectionNode) multipart).addConnections(map);
					}
				});
			}
		}
		return null;
	}

	public <T extends IWorldPosition> ArrayList<T> getConnections(Class<T> classType, boolean includeChannels) {
		ArrayList<T> list = (ArrayList<T>) connections.getOrDefault(classType, (ArrayList<IWorldPosition>) new ArrayList<T>());
		if (includeChannels) {
			ArrayList<Integer> networks = getFinalNetworkList();
			networks.iterator().forEachRemaining(id -> {
				INetworkCache network = Logistics.getNetworkManager().getNetwork(id);
				ArrayList<T> connections = network.getConnections(classType, false);
				connections.iterator().forEachRemaining(connection -> {
					if (!list.contains(connection)) {
						list.add(connection);
					}
				});
			});
		}
		return list;
	}

	@Override
	public ArrayList<NodeConnection> getExternalBlocks(boolean includeChannels) {
		return includeChannels ? networkedTileCache : blockTileCache;
	}

	@Override
	public ArrayList<Entity> getExternalEntities(boolean includeChannels) {
		return includeChannels ? networkedEntityCache : blockEntityCache;
	}

	@Override
	public void refreshCache(int networkID, RefreshType refresh) {
		this.networkID = networkID;
		if (refresh.shouldRefreshCables()) {
			refreshCables();
		}
		if (refresh.shouldRefreshConnections()) {
			refreshConnections();
		}

		if (Logistics.getNetworkManager().updateEmitters) {
			for (IDataReceiver receiver : getConnections(IDataReceiver.class, true)) {
				receiver.refreshConnectedNetworks();
			}
		}

		if (refresh.shouldRefreshNetworks()) {
			refreshNetworks();
			refreshConnections();
		}
		if (refresh.shouldAlertWatchingNetworks()) {
			alertWatchingNetworks();
		}
		lastRefresh = toRefresh;
		toRefresh = RefreshType.NONE;
	}

	/** when a Data Emitter or Data Receiver is changed we need to tell all sub-networks to recheck their connections */
	public void alertWatchingNetworks() {
		ArrayList<Integer> networks = this.getWatchingNetworkList();
		for (Integer id : networks) {
			Logistics.getNetworkManager().markNetworkDirty(id, RefreshType.ALERT);
		}
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

	public void refreshNetworks() {
		ArrayList<Integer> networks = new ArrayList();
		getConnections(IDataReceiver.class, true).iterator().forEachRemaining(receiver -> {
			receiver.getConnectedNetworks().iterator().forEachRemaining(network -> {
				if (!networks.contains(network)) {
					networks.add(network);
				}
			});
		});
		this.connectedNetworks = networks;
	}

	/** this doubles checks all cables for connections as well as finding and local monitors */
	public void refreshConnections() {
		ArrayList<NodeConnection> map = new ArrayList<NodeConnection>(); // array list of pairs so priority is respected
		ArrayList<Entity> entities = Lists.newArrayList();

		ArrayList<IWorldPosition> toRemove = new ArrayList();
		ArrayList<IConnectionNode> nodes = new ArrayList();
		ArrayList<IEntityNode> entitynodes = new ArrayList();

		for (IWorldPosition part : connections.get(IDataCable.class)) {
			if (part == null || !(part instanceof DataCablePart)) {
				toRemove.add(part);
				continue;
			}
			nodes.addAll(CableHelper.getConnectedTiles((DataCablePart) part, IConnectionNode.class));
			entitynodes.addAll(CableHelper.getConnectedTiles((DataCablePart) part, IEntityNode.class));
		}

		Collections.sort(nodes, new Comparator<IConnectionNode>() {
			public int compare(IConnectionNode str1, IConnectionNode str2) {
				return Integer.compare(str2.getPriority(), str1.getPriority());
			}
		});
		nodes.iterator().forEachRemaining(node -> {
			if (!(node instanceof IRemovable) || !((IRemovable) node).wasRemoved()) {
				((IConnectionNode) node).addConnections(map);
			}
		});
		entitynodes.iterator().forEachRemaining(node -> {
			if (!(node instanceof IRemovable) || !((IRemovable) node).wasRemoved()) {
				((IEntityNode) node).addEntities(entities);
			}
		});

		toRemove.forEach(remove -> connections.get(IDataCable.class).remove(remove));
		this.blockTileCache = (ArrayList<NodeConnection>) map.clone();
		this.blockEntityCache = (ArrayList<Entity>) entities.clone();

		ArrayList<Integer> networks = getFinalNetworkList();

		for (Integer id : networks) {
			INetworkCache network = Logistics.getNetworkManager().getNetwork(id);
			ArrayList<NodeConnection> blocks = (ArrayList<NodeConnection>) network.getExternalBlocks(false).clone();
			List<Entity> ents = (List<Entity>) network.getExternalEntities(false).clone();
			for (NodeConnection set : blocks) {
				if (!map.contains(set.coords)) {
					map.add(set);
				}
			}
			for (Entity entity : ents) {
				if (!entities.contains(entity)) {
					entities.add(entity);
				}
			}
			for (ILogicMonitor monitor : network.getLocalMonitors()) {
				if (!localMonitors.contains(monitor)) {
					localMonitors.add(monitor);
				}
			}
		}
		Collections.sort(map, new Comparator<NodeConnection>() {
			public int compare(NodeConnection str1, NodeConnection str2) {
				return Integer.compare(str2.source.getPriority(), str1.source.getPriority());
			}
		});
		this.networkedTileCache = (ArrayList<NodeConnection>) map.clone();
		this.networkedEntityCache = (ArrayList<Entity>) entities.clone();

		monitorInfo.entrySet().forEach(handlers -> compileConnectionList(handlers.getKey()));

		MonitoredList<MonitoredBlockCoords> list = MonitoredList.<MonitoredBlockCoords>newMonitoredList(getNetworkID());
		for (NodeConnection entry : networkedTileCache) {
			TileEntity tile = entry.coords.getTileEntity();
			list.add(new MonitoredBlockCoords(entry.coords, tile != null && tile.getDisplayName() != null ? tile.getDisplayName().getFormattedText() : entry.coords.getBlock().getLocalizedName()));
		}

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
		for (Integer network : connectedNetworks) {
			if (!networks.contains(network)) {
				networks.add(network);
			}
		}
		return networks;
	}

	public ArrayList<Integer> getWatchingNetworkList() {
		ArrayList<Integer> networks = new ArrayList();
		getConnections(IDataEmitter.class, true).iterator().forEachRemaining(emitter -> {
			emitter.getNetworks().iterator().forEachRemaining(network -> {
				if (network != this.networkID && !networks.contains(network)) {
					networks.add(network);
				}
			});
		});
		return networks;
	}

	public ArrayList<Integer> getFinalNetworkList() {
		ArrayList<Integer> networks = getConnectedNetworks(new ArrayList());
		ArrayList<Integer> fullNetworks = (ArrayList<Integer>) networks.clone();
		networks.iterator().forEachRemaining(id -> Logistics.getNetworkManager().getNetwork(id).getConnectedNetworks(fullNetworks));
		return fullNetworks;
	}

	@Override
	public void updateNetwork(int networkID) {
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
		for (Entry<LogicMonitorHandler, Map<ILogicMonitor, MonitoredList<?>>> monitorMap : monitorInfo.entrySet()) {
			Map<NodeConnection, MonitoredList<?>> newTileConnections;
			Map<Entity, MonitoredList<?>> newEntityConnections;
			if (!monitorMap.getValue().isEmpty() && monitorMap.getKey() != null) {
				if (monitorMap.getKey().id() == InfoMonitorHandler.id) {

					ArrayList<BlockCoords> toUpdate = new ArrayList();
					for (Entry<ILogicMonitor, MonitoredList<?>> monitors : monitorMap.getValue().entrySet()) {
						ILogicMonitor monitor = monitors.getKey();
						if (!monitor.getViewersList().getViewers(true, ViewerType.FULL_INFO, ViewerType.TEMPORARY).isEmpty()) {
							IdentifiedCoordsList list = monitor.getChannels(0);
							for (BlockCoords coord : list) {
								if (!toUpdate.contains(coord))
									toUpdate.add(coord);
							}
						}
					}
					
					
					// TODO VERSION FOR ENTITIES!!
					newTileConnections = getTileMonitoredList(monitorMap.getKey(), toUpdate);
					newEntityConnections = entityConnectionInfo.getOrDefault(monitorMap.getKey(), new LinkedHashMap());
				} else {
					newTileConnections = getTileMonitoredList(monitorMap.getKey()); // individual MonitoredLists for every connection
					newEntityConnections = getEntityMonitoredList(monitorMap.getKey());
				}
				if (monitorMap.getKey() instanceof ITileMonitorHandler)
					tileConnectionInfo.replace((ITileMonitorHandler) monitorMap.getKey(), newTileConnections);// the connectionInfo is saved.
				if (monitorMap.getKey() instanceof IEntityMonitorHandler)
					entityConnectionInfo.replace((IEntityMonitorHandler) monitorMap.getKey(), newEntityConnections);// the connectionInfo is saved.

				updateAndSendLists(monitorMap, newTileConnections, newEntityConnections);
			}
		}
		resendAllLists = false;
	}
	

	public void updateAndSendLists(Entry<LogicMonitorHandler, Map<ILogicMonitor, MonitoredList<?>>> monitorMap, Map<NodeConnection, MonitoredList<?>> newTileConnections, Map<Entity, MonitoredList<?>> newEntityConnections) {
		for (Entry<ILogicMonitor, MonitoredList<?>> monitors : monitorMap.getValue().entrySet()) {
			ILogicMonitor monitor = monitors.getKey();
			if (monitor != null && monitor.getHandler().id().equals(monitorMap.getKey().id())) {
				
				ArrayList<NodeConnection> nodeConnections = new ArrayList();
				ArrayList<Entity> entityConnections = new ArrayList();
				MonitoredList updateList = updateMonitoredList(monitors.getKey(), 0, newTileConnections, newEntityConnections, nodeConnections, entityConnections).updateList(monitors.getValue());		
				monitor.setMonitoredInfo(updateList, nodeConnections, entityConnections, 0);// TODO only one channel atm!
				sendPacketsToViewers(monitor, updateList.copyInfo(), monitors.getValue().copyInfo());
				monitors.setValue(updateList);
				Logistics.getServerManager().monitoredLists.put(new InfoUUID(monitor.getIdentity().hashCode(), 0), updateList);
				
			} else if (!monitor.getHandler().id().equals(monitorMap.getKey().id())) {
				Logistics.logger.info("WRONG MONITOR HANDLER FOR MONITOR: " + monitorMap.getKey().id());
			}
		}
	}

	public <T extends IMonitorInfo> void compileConnectionList(LogicMonitorHandler<T> type) {
		if (type instanceof ITileMonitorHandler) {
			HashMap<NodeConnection, MonitoredList<?>> compiledList = new HashMap();
			for (NodeConnection pair : networkedTileCache) {
				if (!compiledList.containsKey(pair))
					compiledList.put(pair, MonitoredList.<T>newMonitoredList(getNetworkID()));
			}
			tileConnectionInfo.put((ITileMonitorHandler) type, compiledList);
		}
		if (type instanceof IEntityMonitorHandler) {
			HashMap<Entity, MonitoredList<?>> compiledList = new HashMap();
			for (Entity entity : networkedEntityCache) {
				if (!compiledList.containsKey(entity))
					compiledList.put(entity, MonitoredList.<T>newMonitoredList(getNetworkID()));
			}
			entityConnectionInfo.put((IEntityMonitorHandler) type, compiledList);
		}
	}

	@Override
	public boolean isFakeNetwork() {
		return false;
	}

	@Override
	public void addLocalMonitor(ILogicMonitor monitor) {
		localMonitors.add(monitor);
	}

	@Override
	public ILogicMonitor getLocalMonitor() {
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
	public ArrayList<ILogicMonitor> getLocalMonitors() {
		return localMonitors;
	}

	@Override
	public RefreshType getLastRefresh() {
		return lastRefresh;
	}

}