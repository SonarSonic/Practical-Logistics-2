package sonar.logistics.connections.channels;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.PlayerListener;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.types.MonitoredBlockCoords;

public abstract class ListNetworkChannels<M extends IInfo, H extends INetworkListHandler> extends DefaultNetworkChannels<H> implements INetworkListChannels<H> {

	public final H handler;
	public List<IListReader<M>> readers = Lists.newArrayList();
	protected Iterator<IListReader<M>> readerIterator;
	protected int readersPerTick = 0;

	public Map<NodeConnection, MonitoredList<M>> channels = Maps.newHashMap();
	protected Iterator<Entry<NodeConnection, MonitoredList<M>>> channelIterator;
	protected int channelsPerTick = 0;

	protected boolean hasListeners = false;

	protected ListNetworkChannels(H handler, ILogisticsNetwork network) {
		super(network, CacheHandler.READER);
		this.handler = handler;
	}

	@Override
	public H getHandler() {
		return handler;
	}

	@Override
	public int getUpdateRate() {
		return handler.updateRate();
	}

	@Override
	public void updateChannel() {
		super.updateChannel();
		if (hasListeners) {
			updateChannels();
			updateReaders(true);
		}
	}

	protected void updateTicks() {
		super.updateTicks();
		hasListeners = false;
		for (IListReader reader : readers) {
			if (reader.getListenerList().hasListeners()) {
				hasListeners = true;
				break;
			}
		}
		if (hasListeners) {
			updateTickLists();
		}
	}

	public void updateTickLists() {
		this.readersPerTick = readers.size() > handler.updateRate() ? (int) Math.ceil(readers.size() / Math.max(1, handler.updateRate())) : 1;
		this.channelsPerTick = channels.size() > handler.updateRate() ? (int) Math.ceil(channels.size() / Math.max(1, handler.updateRate())) : 1;
		
		this.channelIterator = channels.entrySet().iterator();
		this.readerIterator = readers.iterator();
	}

	@Override
	public void addConnection(CacheHandler cache, INetworkListener connection) {
		// TODO check the cache handler before adding, in this situation it will always be a reader
		IListReader reader = (IListReader) connection;
		if (reader.getValidHandlers().contains(handler)) {
			if (!readers.contains(reader) && readers.add(reader)) {
				onChannelsChanged();
				updateTicks();
			}
		}
	}

	@Override
	public void removeConnection(CacheHandler cache, INetworkListener connection) {
		if (readers.remove(connection)) {
			onChannelsChanged();
			updateTicks();
		}
	}

	@Override
	public void onChannelsChanged() {
		channels = handler.getAllChannels(Maps.newHashMap(), channels, network);
		updateTicks();
	}

	public void updateChannels() {
		int used = 0;
		while (channelIterator.hasNext() && used != channelsPerTick) {
			Entry<NodeConnection, MonitoredList<M>> entry = channelIterator.next();
			MonitoredList<M> oldList = entry.getValue() == null ? MonitoredList.<M>newMonitoredList(network.getNetworkID()) : entry.getValue();
			MonitoredList<M> newList = handler.updateConnection(this, MonitoredList.<M>newMonitoredList(network.getNetworkID()), oldList, entry.getKey());
			channels.put(entry.getKey(), newList);
			used++;
		}
	}

	public void updateReaders(boolean send) {
		int used = 0;
		while (readerIterator.hasNext() && used != readersPerTick) {
			IListReader<M> reader = readerIterator.next();
			if (reader.getListenerList().hasListeners()) {
				handler.updateAndSendList(network, reader, channels, send);
			}
			used++;
		}
	}

	public void updateAllChannels() {
		for (Entry<NodeConnection, MonitoredList<M>> entry : channels.entrySet()) {
			MonitoredList<M> oldList = entry.getValue() == null ? MonitoredList.<M>newMonitoredList(network.getNetworkID()) : entry.getValue();
			channels.put(entry.getKey(), handler.updateConnection(this, MonitoredList.<M>newMonitoredList(network.getNetworkID()), oldList, entry.getKey()));
		}
	}

	public void updateAllReaders(boolean send) {
		readers.forEach(reader -> handler.updateAndSendList(network, reader, channels, send));
	}

	public void sendFullRapidUpdate() {
		updateAllChannels();
		updateAllReaders(true);
	}

	public void sendLocalRapidUpdate(IListReader<M> reader, EntityPlayer viewer) {
		PlayerListener listener = reader.getListenerList().findListener(viewer);
		if (listener != null) {
			updateAllChannels();
			Pair<InfoUUID, MonitoredList<M>> list = handler.updateAndSendList(network, reader, channels, false);
			PacketHelper.sendReaderFullInfo(Lists.newArrayList(listener), reader, list.b, list.a);
		}
	}

	@Override
	public void onDeleted() {
		super.onDeleted();
		readers.clear();
		readerIterator = null;
		channels.clear();
		channelIterator = null;
	}

	@Override
	public void onCreated() {}

	@Override
	public boolean isCoordsMonitored(BlockConnection connection) {
		return true;
	}

	@Override
	public boolean isEntityMonitored(EntityConnection connection) {
		return true;
	}
}
