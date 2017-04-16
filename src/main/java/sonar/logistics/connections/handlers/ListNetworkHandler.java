package sonar.logistics.connections.handlers;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.nodes.NodeConnectionType;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.helpers.PacketHelper;

public abstract class ListNetworkHandler<I extends IInfo> implements INetworkListHandler<I> {

	Boolean tiles, entities;

	public int getReaderID(IListReader reader) {
		return 0;
	}

	public boolean canHandleTiles() {
		if (tiles == null) {
			tiles = this instanceof ITileMonitorHandler;
		}
		return tiles;
	}

	public boolean canHandleEntities() {
		if (entities == null) {
			entities = this instanceof IEntityMonitorHandler;
		}
		return entities;
	}

	public boolean canHandle(NodeConnection connection) {
		NodeConnectionType type = connection.getType();
		switch (type) {
		case ENTITY:
			return canHandleEntities();
		case TILE:
			return canHandleTiles();
		default:
			return false;
		}
	}

	public Map<NodeConnection, MonitoredList<I>> getAllChannels(Map<NodeConnection, MonitoredList<I>> compiling, Map<NodeConnection, MonitoredList<I>> oldList, ILogisticsNetwork network) {
		for (NodeConnection connection : network.getConnections(CacheType.ALL)) {
			if (network.validateTile(connection.source) && canHandle(connection) && !compiling.containsKey(connection)) {
				compiling.put(connection, oldList.getOrDefault(connection, MonitoredList.newMonitoredList(network.getNetworkID())));
			}
		}
		return compiling;
	}

	public MonitoredList<I> updateConnection(INetworkListChannels channels, MonitoredList<I> newList, MonitoredList<I> oldList, NodeConnection c) {
		if (c instanceof BlockConnection) {
			BlockConnection connection = (BlockConnection) c;
			if (channels.isCoordsMonitored(connection)) {
				return ((ITileMonitorHandler) this).updateInfo(channels, newList, oldList, connection);
			}
		} else if (c instanceof EntityConnection) {
			EntityConnection connection = (EntityConnection) c;
			if (channels.isEntityMonitored(connection)) {
				return ((IEntityMonitorHandler) this).updateInfo(channels, newList, oldList, connection);
			}
		}
		return oldList;
	}

	public Pair<InfoUUID, MonitoredList<I>> updateAndSendList(ILogisticsNetwork network, IListReader<I> reader, Map<NodeConnection, MonitoredList<I>> channelLists, boolean send) {
		InfoUUID uuid = new InfoUUID(reader.getIdentity(), getReaderID(reader));
		if (network.validateTile(reader)) {
			List<NodeConnection> usedChannels = Lists.newArrayList();
			MonitoredList lastList = PL2.getServerManager().monitoredLists.getOrDefault(uuid, MonitoredList.newMonitoredList(network.getNetworkID()));
			MonitoredList updateList = reader.getUpdatedList(uuid, channelLists, usedChannels).updateList(lastList);
			if (reader instanceof INetworkReader) {
				((INetworkReader) reader).setMonitoredInfo(updateList, usedChannels, uuid);
			}
			PL2.getServerManager().monitoredLists.put(uuid, updateList);
			if (send)
				PacketHelper.sendPacketsToListeners(reader, updateList, lastList, uuid);
			return new Pair(uuid, updateList);
		}
		return new Pair(uuid, MonitoredList.newMonitoredList(network.getNetworkID()));
	}
}
