package sonar.logistics.networking.subnetworks;

import sonar.core.utils.Pair;
import sonar.logistics.PL2Events;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.info.register.RegistryType;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.networking.ILogisticsNetwork;
import sonar.logistics.api.networking.INetworkChannels;
import sonar.logistics.api.networking.INetworkHandler;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.registries.LogisticsInfoRegistry;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.networking.CacheHandler;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.cabling.CableConnectionHandler;

import java.util.ArrayList;
import java.util.List;

public class NetworkWatcherHandler implements INetworkHandler {

	public static NetworkWatcherHandler INSTANCE = new NetworkWatcherHandler();

	@Override
	public int updateRate() {
		return 20;
	}

	@Override
	public Class<? extends INetworkChannels> getChannelsType() {
		return NetworkWatcherChannels.class;
	}

	private InfoChangeableList newChangeableList() {
		return new InfoChangeableList();
	}

	public AbstractChangeableList<IProvidableInfo> updateNetworkList(AbstractChangeableList<IProvidableInfo> list, ILogisticsNetwork network) {
		double pl2Tick = PL2Events.updateTick / 1000000.0;
		double networkTick = network.getNetworkTickTime() / 1000000.0;
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_TICK_TIME, RegistryType.LOGICNETWORK, pl2Tick));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_TICK_PERCENT, RegistryType.LOGICNETWORK, ((pl2Tick) / 50) * 100)); // 50 milliseconds in a tick
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_TICK_FURNACE, RegistryType.LOGICNETWORK, (PL2Events.updateTick / 1000) / 20)); // 50 milliseconds in a tick
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_NETWORK_TICK_TIME, RegistryType.LOGICNETWORK, networkTick));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_NETWORK_TICK_PERCENT, RegistryType.LOGICNETWORK, ((networkTick) / 50) * 100));// 50 milliseconds in a tick
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_NETWORK_TICK_FURNACE, RegistryType.LOGICNETWORK, (network.getNetworkTickTime() / 1000) / 20)); // 50 milliseconds in a tick
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_NETWORK_ID, RegistryType.LOGICNETWORK, network.getNetworkID()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CONNECTED_NETWORKS, RegistryType.LOGICNETWORK, network.getListenerList().getListeners(ILogisticsNetwork.CONNECTED_NETWORK).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_WATCHING_NETWORKS, RegistryType.LOGICNETWORK, network.getListenerList().getListeners(ILogisticsNetwork.WATCHING_NETWORK).size()));

		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CACHE_CABLES, RegistryType.LOGICNETWORK, CableConnectionHandler.instance().getConnections(network.getNetworkID()).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CACHE_TILES, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.TILE, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CACHE_NODES, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.NODES, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CACHE_ENTITY_NODES, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.ENTITY_NODES, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CACHE_READERS, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.READER, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CACHE_EMITTERS, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.EMITTERS, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CACHE_RECEIVERS, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.RECEIVERS, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_CACHE_TRANSFER_NODES, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.TRANSFER_NODES, CacheType.ALL).size()));

		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_TOTAL_CONNECTIONS, RegistryType.LOGICNETWORK, network.getConnections(CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_LOCAL_CONNECTIONS, RegistryType.LOGICNETWORK, network.getConnections(CacheType.LOCAL).size()));
		list.add(LogicInfo.buildDirectInfo(LogisticsInfoRegistry.PL_GLOBAL_CONNECTIONS, RegistryType.LOGICNETWORK, network.getConnections(CacheType.GLOBAL).size()));
		return list;
	}

	public Pair<InfoUUID, AbstractChangeableList<IProvidableInfo>> updateAndSendList(ILogisticsNetwork network, IListReader<IProvidableInfo> reader, AbstractChangeableList<IProvidableInfo> networkList, boolean send) {
		InfoUUID uuid = getReaderUUID(reader);
		if (network.validateTile(reader)) {
			List<NodeConnection> usedChannels = new ArrayList<>();
			AbstractChangeableList<IProvidableInfo> currentList = ServerInfoHandler.instance().getMonitoredList(uuid);
			
			final AbstractChangeableList<IProvidableInfo> updateList = currentList == null ? newChangeableList() : currentList;
			updateList.saveStates();
			networkList.createSaveableList().forEach(updateList::add);
			if (reader instanceof INetworkReader) {
				((INetworkReader) reader).setMonitoredInfo(updateList, usedChannels, uuid);
			}
			ServerInfoHandler.instance().monitoredLists.put(uuid, updateList);
			if (send && (!updateList.wasLastListNull || updateList.wasLastListNull != updateList.getList().isEmpty()))
				PacketHelper.sendReaderToListeners(reader, updateList, uuid);
			return new Pair(uuid, updateList);
		}
		return new Pair(uuid, newChangeableList());
	}
}
