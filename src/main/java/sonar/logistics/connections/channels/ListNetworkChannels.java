package sonar.logistics.connections.channels;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.helpers.LogisticsHelper;

public class ListNetworkChannels<M extends IMonitorInfo, H extends INetworkListHandler<M>> extends DefaultNetworkChannels<H> implements INetworkListChannels<H> {

	private List<IListReader<M>> readers = Lists.newArrayList();
	private Iterator<IListReader<M>> readerIterator;
	private int readersPerTick = 0;

	private Map<NodeConnection, MonitoredList<M>> channels = Maps.newHashMap();
	private Iterator<Entry<NodeConnection, MonitoredList<M>>> channelIterator;
	private ChannelList currentList;
	private int channelsPerTick = 0;

	public boolean hasListeners = false;
	
	public ListNetworkChannels(H handler, ILogisticsNetwork network) {
		super(handler, network, CacheHandler.READER);
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
			this.currentList = handler.getChannelsList(network, readers);
			this.readersPerTick = readers.size() > handler.updateRate() ? (int) Math.ceil(readers.size() / Math.max(1, handler.updateRate())) : 1;
			this.channelsPerTick = channels.size() > handler.updateRate() ? (int) Math.ceil(channels.size() / Math.max(1, handler.updateRate())) : 1;
			this.channelIterator = channels.entrySet().iterator();
			this.readerIterator = readers.iterator();
		}
	}
	

	@Override
	public void updateChannelLists() {
		super.updateChannelLists();
		if (hasListeners) {
			updateChannels();
			updateReaders();
		}
	}

	@Override
	public void addConnection(CacheHandler cache, INetworkListener connection) {
		// TODO check the cache handler before adding, in this situation it will always be a reader
		IListReader reader = (IListReader) connection;
		if (reader.getValidHandlers().contains(handler)) {
			if (!readers.contains(reader) && readers.add(reader)) {
				createChannelLists();
				updateTicks();
			}
		}
	}

	@Override
	public void removeConnection(CacheHandler cache, INetworkListener connection) {
		if (readers.remove(connection)) {
			createChannelLists();
			updateTicks();
		}
	}

	@Override
	public void createChannelLists() {		
		channels = handler.getAllChannels(Maps.newHashMap(), network);
		updateTicks();
	}

	private void updateChannels() {
		int used = 0;
		while (channelIterator.hasNext() && used != channelsPerTick) {
			Entry<NodeConnection, MonitoredList<M>> entry = channelIterator.next();
			MonitoredList<M> oldList = entry.getValue() == null ? MonitoredList.<M>newMonitoredList(network.getNetworkID()) : entry.getValue();
			MonitoredList<M> newList = handler.updateConnection(MonitoredList.<M>newMonitoredList(network.getNetworkID()), oldList, entry.getKey(), currentList);			
			entry.setValue(newList);
			used++;
		}
	}

	private void updateReaders() {
		int used = 0;
		while (readerIterator.hasNext() && used != readersPerTick) {
			IListReader<M> reader = readerIterator.next();
			if (reader.getListenerList().hasListeners()) {
				handler.updateAndSendList(network, reader, channels, true);
			}
			used++;
		}
	}

	private void updateAllChannels() {
		for (Entry<NodeConnection, MonitoredList<M>> entry : channels.entrySet()) {
			MonitoredList<M> oldList = entry.getValue() == null ? MonitoredList.<M>newMonitoredList(network.getNetworkID()) : entry.getValue();
			entry.setValue(handler.updateConnection(MonitoredList.<M>newMonitoredList(network.getNetworkID()), oldList, entry.getKey(), currentList));
		}
	}

	private void updateAllReaders(boolean send) {
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
			LogisticsHelper.sendFullInfo(Lists.newArrayList(listener), reader, list.b, list.a);
		}
	}

	@Override
	public void onDeleted() {
		super.onDeleted();
		readers.clear();
		readerIterator = null;
		channels.clear();
		channelIterator = null;
		currentList = null;
	}

	@Override
	public void onCreated() {}
}
