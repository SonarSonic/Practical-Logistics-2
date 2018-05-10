package sonar.logistics.core.tiles.readers.network.handling;

import sonar.core.utils.Pair;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.INetworkReader;
import sonar.logistics.api.core.tiles.readers.channels.INetworkChannels;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.events.PL2Events;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.core.tiles.connections.data.handling.CableConnectionHandler;
import sonar.logistics.core.tiles.connections.data.network.CacheHandler;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;
import sonar.logistics.core.tiles.displays.info.types.general.LogicInfo;
import sonar.logistics.integration.pl2.PL2InfoRegistry;

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
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_TICK_TIME, RegistryType.LOGICNETWORK, pl2Tick));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_TICK_PERCENT, RegistryType.LOGICNETWORK, ((pl2Tick) / 50) * 100)); // 50 milliseconds in a tick
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_TICK_FURNACE, RegistryType.LOGICNETWORK, (PL2Events.updateTick / 1000) / 20)); // 50 milliseconds in a tick
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_NETWORK_TICK_TIME, RegistryType.LOGICNETWORK, networkTick));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_NETWORK_TICK_PERCENT, RegistryType.LOGICNETWORK, ((networkTick) / 50) * 100));// 50 milliseconds in a tick
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_NETWORK_TICK_FURNACE, RegistryType.LOGICNETWORK, (network.getNetworkTickTime() / 1000) / 20)); // 50 milliseconds in a tick
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_NETWORK_ID, RegistryType.LOGICNETWORK, network.getNetworkID()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CONNECTED_NETWORKS, RegistryType.LOGICNETWORK, network.getListenerList().getListeners(ILogisticsNetwork.CONNECTED_NETWORK).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_WATCHING_NETWORKS, RegistryType.LOGICNETWORK, network.getListenerList().getListeners(ILogisticsNetwork.WATCHING_NETWORK).size()));

		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CACHE_CABLES, RegistryType.LOGICNETWORK, CableConnectionHandler.instance().getConnections(network.getNetworkID()).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CACHE_TILES, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.TILE, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CACHE_NODES, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.NODES, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CACHE_ENTITY_NODES, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.ENTITY_NODES, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CACHE_READERS, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.READER, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CACHE_EMITTERS, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.EMITTERS, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CACHE_RECEIVERS, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.RECEIVERS, CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_CACHE_TRANSFER_NODES, RegistryType.LOGICNETWORK, network.getCachedTiles(CacheHandler.TRANSFER_NODES, CacheType.ALL).size()));

		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_TOTAL_CONNECTIONS, RegistryType.LOGICNETWORK, network.getConnections(CacheType.ALL).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_LOCAL_CONNECTIONS, RegistryType.LOGICNETWORK, network.getConnections(CacheType.LOCAL).size()));
		list.add(LogicInfo.buildDirectInfo(PL2InfoRegistry.PL_GLOBAL_CONNECTIONS, RegistryType.LOGICNETWORK, network.getConnections(CacheType.GLOBAL).size()));
		return list;
	}

	public Pair<InfoUUID, AbstractChangeableList<IProvidableInfo>> updateAndSendList(ILogisticsNetwork network, IListReader<IProvidableInfo> reader, AbstractChangeableList<IProvidableInfo> networkList, boolean send) {
		InfoUUID uuid = getReaderUUID(reader);
		if (network.validateTile(reader)) {
			List<NodeConnection> usedChannels = new ArrayList<>();
			AbstractChangeableList<IProvidableInfo> currentList = ServerInfoHandler.instance().getChangeableListMap().getOrDefault(uuid, newChangeableList());
			currentList.saveStates();
			networkList.createSaveableList().forEach(currentList::add);
			if (reader instanceof INetworkReader) {
				((INetworkReader) reader).setMonitoredInfo(currentList, usedChannels, uuid);
			}
			ServerInfoHandler.instance().getChangeableListMap().put(uuid, currentList);
			if (send && (!currentList.wasLastListNull || currentList.wasLastListNull != currentList.getList().isEmpty()))
				InfoPacketHelper.sendReaderToListeners(reader, currentList, uuid);
			return new Pair(uuid, currentList);
		}
		return new Pair(uuid, newChangeableList());
	}
}
