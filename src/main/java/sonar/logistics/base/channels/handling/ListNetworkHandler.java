package sonar.logistics.base.channels.handling;

import sonar.core.utils.Pair;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.INetworkReader;
import sonar.logistics.api.core.tiles.readers.channels.IEntityMonitorHandler;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListChannels;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListHandler;
import sonar.logistics.api.core.tiles.readers.channels.ITileMonitorHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.channels.NodeConnectionType;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ListNetworkHandler<I extends IInfo, L extends AbstractChangeableList> implements INetworkListHandler<I, L> {

	Boolean tiles, entities;

	public int getReaderID(IListReader reader) {
		return 0;
	}
	
	public InfoUUID getReaderUUID(IListReader reader){
		return new InfoUUID(reader.getIdentity(), getReaderID(reader));
	}
	
	public AbstractChangeableList<I> getUUIDLatestList(InfoUUID uuid){
		return ServerInfoHandler.instance().getChangeableListMap().getOrDefault(uuid, newChangeableList());
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
				return (L) ((ITileMonitorHandler) this).updateInfo(channels, newList, connection);
			}
		} else if (c instanceof EntityConnection) {
			EntityConnection connection = (EntityConnection) c;
			if (channels.isEntityMonitored(connection)) {
				newList.saveStates();
				return (L) ((IEntityMonitorHandler) this).updateInfo(channels, newList, connection);
			}
		}
		return newList;
	}

	public Pair<InfoUUID, AbstractChangeableList<I>> updateAndSendList(ILogisticsNetwork network, IListReader<I> reader, Map<NodeConnection, AbstractChangeableList<I>> channelLists, boolean send) {
		InfoUUID uuid = getReaderUUID(reader);
		if (network.validateTile(reader)) {
			List<NodeConnection> usedChannels = new ArrayList<>();
			AbstractChangeableList<I> updateList = getUUIDLatestList(uuid);
			updateList.saveStates();
			AbstractChangeableList<I> viewableList = reader.getViewableList(updateList, uuid, channelLists, usedChannels);
			if (reader instanceof INetworkReader) {
				((INetworkReader) reader).setMonitoredInfo(updateList, usedChannels, uuid);
			}
			ServerInfoHandler.instance().getChangeableListMap().put(uuid, updateList);
			if (send && (!updateList.wasLastListNull || updateList.wasLastListNull != updateList.getList().isEmpty()))
				InfoPacketHelper.sendReaderToListeners(reader, updateList, uuid);
			return new Pair(uuid, updateList);
		}
		return new Pair(uuid, newChangeableList());
	}
}
