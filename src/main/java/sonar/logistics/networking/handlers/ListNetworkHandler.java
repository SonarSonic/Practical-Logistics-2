package sonar.logistics.networking.handlers;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.IMonitoredValueInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
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
import sonar.logistics.helpers.PacketHelper;

public abstract class ListNetworkHandler<I extends IInfo, L extends AbstractChangeableList> implements INetworkListHandler<I, L> {

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

	public Map<NodeConnection, L> getAllChannels(Map<NodeConnection, L> list, ILogisticsNetwork network) {
		for (NodeConnection connection : network.getConnections(CacheType.ALL)) {
			if (network.validateTile(connection.source) && canHandle(connection) && !list.containsKey(connection)) {
				list.put(connection, newChangeableList());
			}
		}
		return list;
	}

	public L updateConnection(INetworkListChannels channels, L newList, NodeConnection c) {
		if (c instanceof BlockConnection) {
			BlockConnection connection = (BlockConnection) c;
			if (channels.isCoordsMonitored(connection)) { // isn't monitored???
				newList.saveStates();
				L info = (L) ((ITileMonitorHandler) this).updateInfo(channels, newList, connection);
				return info;
			}
		} else if (c instanceof EntityConnection) {
			EntityConnection connection = (EntityConnection) c;
			if (channels.isEntityMonitored(connection)) {
				newList.saveStates();
				L info = (L) ((IEntityMonitorHandler) this).updateInfo(channels, newList, connection);
				return info;
			}
		}
		return newList;
	}

	public Pair<InfoUUID, L> updateAndSendList(ILogisticsNetwork network, IListReader<I> reader, Map<NodeConnection, L> channelLists, boolean send) {
		InfoUUID uuid = new InfoUUID(reader.getIdentity(), getReaderID(reader));
		if (network.validateTile(reader)) {
			List<NodeConnection> usedChannels = Lists.newArrayList();
			AbstractChangeableList<I> updateList = (updateList = PL2.getServerManager().getMonitoredList(uuid)) == null ? this.newChangeableList() : updateList;
			updateList.saveStates();
			AbstractChangeableList<I> viewableList = reader.getViewableList(updateList, uuid, (Map<NodeConnection, AbstractChangeableList<I>>) channelLists, usedChannels);
			if (reader instanceof INetworkReader) {
				((INetworkReader) reader).setMonitoredInfo(updateList, usedChannels, uuid);
			}
			PL2.getServerManager().monitoredLists.put(uuid, updateList);
			if (send)
				PacketHelper.sendReaderToListeners(reader, updateList, uuid);
			return new Pair(uuid, updateList);
		}
		return new Pair(uuid, newChangeableList());
	}
}
