package sonar.logistics.networking.subnetworks;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.networking.CacheHandler;
import sonar.logistics.networking.common.DefaultNetworkChannels;

public class NetworkWatcherChannels extends DefaultNetworkChannels {

	public List<IListReader<IProvidableInfo>> readers = Lists.newArrayList();
	protected Iterator<IListReader<IProvidableInfo>> readerIterator;
	public AbstractChangeableList<IProvidableInfo> networkList = new InfoChangeableList();
	public boolean shouldRapidUpdate;
	protected int readersPerTick = 0;

	protected boolean hasListeners = false;

	public NetworkWatcherChannels(ILogisticsNetwork network) {
		super(network, CacheHandler.READER);
	}

	public NetworkWatcherHandler handler() {
		return NetworkWatcherHandler.INSTANCE;
	}

	@Override
	public void updateChannel() {
		super.updateChannel();
		if (hasListeners) {
			updateNetworkList();
			updateReaders(true);
		}
	}

	protected void tickChannels() {
		super.tickChannels();
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
		this.readersPerTick = readers.size() > handler().updateRate() ? (int) Math.ceil(readers.size() / Math.max(1, handler().updateRate())) : 1;
		this.readerIterator = readers.iterator();
	}

	@Override
	public void addConnection(CacheHandler cache, INetworkListener connection) {
		IListReader reader = (IListReader) connection;
		if (reader.getValidHandlers().contains(handler())) {
			if (!readers.contains(reader) && readers.add(reader)) {
				onChannelsChanged();
				tickChannels();
			}
		}
	}

	@Override
	public void removeConnection(CacheHandler cache, INetworkListener connection) {
		if (readers.remove(connection)) {
			onChannelsChanged();
			tickChannels();
		}
	}
	
	public void updateNetworkList() {
		networkList = handler().updateNetworkList(networkList, network);
	}

	public void updateReaders(boolean send) {
		int used = 0;
		while (readerIterator.hasNext() && used != readersPerTick) {
			IListReader<IProvidableInfo> reader = readerIterator.next();
			if (reader.getListenerList().hasListeners()) {
				handler().updateAndSendList(network, reader, networkList, send);
			}
			used++;
		}
	}

	public void updateAllReaders(boolean send) {
		readers.forEach(reader -> handler().updateAndSendList(network, reader, networkList, send));
	}

	@Override
	public void onCreated() {

	}

	@Override
	public void onDeleted() {
		super.onDeleted();
		readers.clear();
		readerIterator = null;
	}

	@Override
	public int getUpdateRate() {
		return handler().updateRate();
	}

}
