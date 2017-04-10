package sonar.logistics.connections.handlers;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.utils.NodeConnectionType;
import sonar.logistics.connections.channels.ListNetworkChannels;
import sonar.logistics.helpers.LogisticsHelper;

public abstract class ListNetworkHandler<I extends IMonitorInfo> extends DefaultNetworkHandler implements INetworkListHandler<I> {

	Boolean tiles, entities;

	public @Nullable ChannelList getChannelsList(ILogisticsNetwork network, List<IListReader<I>> readers) {
		return null;
	}

	public int getReaderID(IListReader reader) {
		return 0;
	}

	@Override
	public INetworkChannels instance(ILogisticsNetwork network) {
		return new ListNetworkChannels(this, network);
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

	public Map<NodeConnection, MonitoredList<I>> getAllChannels(Map<NodeConnection, MonitoredList<I>> compiling, ILogisticsNetwork network) {
		for (NodeConnection connection : network.getChannels(CacheType.ALL)) {// TODO check this is working pls
			if (network.validateTile(connection.source) && canHandle(connection) && !compiling.containsKey(connection)) {
				MonitoredList<I> list = compiling.getOrDefault(connection, MonitoredList.newMonitoredList(network.getNetworkID()));
				compiling.put(connection, list);
			}
		}
		return compiling;
	}

	public MonitoredList<I> updateConnection(MonitoredList<I> newList, MonitoredList<I> oldList, NodeConnection c, ChannelList channels) {
		if (c instanceof BlockConnection) {
			BlockConnection connection = (BlockConnection) c;
			if (channels == null || channels.isCoordsMonitored(connection.coords)) { // TODO check this in the INetworkChannels
				return ((ITileMonitorHandler) this).updateInfo(newList, oldList, connection);
			}
		} else if (c instanceof EntityConnection) {
			EntityConnection connection = (EntityConnection) c;
			if (channels == null || channels.isEntityMonitored(connection.entity.getPersistentID())) {
				return ((IEntityMonitorHandler) this).updateInfo(newList, oldList, connection);
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
				LogisticsHelper.sendPacketsToListeners(reader, updateList, lastList, uuid);
			return new Pair(uuid, updateList);
		}
		return new Pair(uuid, MonitoredList.newMonitoredList(network.getNetworkID()));
	}
}
