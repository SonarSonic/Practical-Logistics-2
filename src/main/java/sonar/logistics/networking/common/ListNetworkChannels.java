package sonar.logistics.networking.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.viewers.ListenerType.UpdateType;
import sonar.logistics.api.viewers.UpdateListenerList;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.CacheHandler;

public abstract class ListNetworkChannels<M extends IInfo, H extends INetworkListHandler> extends DefaultNetworkChannels implements INetworkListChannels<H> {

	public final H handler;
	public List<IListReader<M>> readers = new ArrayList<>();
	protected Iterator<IListReader<M>> readerIterator;
	public boolean shouldRapidUpdate;
	protected int readersPerTick = 0;

	public Map<Integer, List<NodeConnection>> usedChannels = new HashMap<>();// identity of the reader and the channels it uses
	public Map<NodeConnection, AbstractChangeableList<M>> channels = new HashMap<>();
	protected Iterator<Entry<NodeConnection, AbstractChangeableList<M>>> channelIterator;
	protected int channelsPerTick = 0;

	protected UpdateListenerList UPDATES = new UpdateListenerList();

	protected ListNetworkChannels(H handler, ILogisticsNetwork network) {
		super(network, CacheHandler.READER);
		this.handler = handler;
	}

	public void updateListenerList() {
		UPDATES.reset();
		readers: for (IListReader reader : readers) {
			if (!UPDATES.canAccept()) {
				break;
			}
			List<ListenerTally<PlayerListener>> listeners = reader.getListenerList().getTallies(ListenerType.values());
			for (ListenerTally t : listeners) {
				int[] tallies = t.tallies;
				for (int i = 0; i < tallies.length; i++) {
					int tally = tallies[i];
					if (tally > 0) {
						UPDATES.add(ListenerType.values()[i]);
					}
				}
				if (!UPDATES.canAccept()) {
					break readers;
				}
			}
			//FIXME should this be necessary?
			if(reader.getListenerList().getDisplayListeners().hasListeners()){
				UPDATES.add(ListenerType.OLD_DISPLAY_LISTENER);
			}
		}
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
		updateListenerList();
		super.updateChannel();
		runUpdates();
	}

	/** override this to change the behavoir of ticks based on listeners */
	public void runUpdates() {
		if (UPDATES.canSyncUpdate(UpdateType.GUI, UpdateType.DISPLAY)) {
			updateChannels();
			updateReaders(true);
		}
	}

	public void runChannelUpdates() {
		//if (UPDATES.canSyncUpdate(UpdateType.GUI, UpdateType.DISPLAY)) {
			updateTickLists();
		//}
	}

	protected void tickChannels() {
		super.tickChannels();
		runChannelUpdates();
	}

	public void updateTickLists() {
		this.readersPerTick = readers.size() > handler.updateRate() ? (int) Math.ceil(readers.size() / Math.max(1, handler.updateRate())) : 1;
		this.channelsPerTick = channels.size() > handler.updateRate() ? (int) Math.ceil(channels.size() / Math.max(1, handler.updateRate())) : 1;

		this.channelIterator = channels.entrySet().iterator();
		this.readerIterator = readers.iterator();
	}

	@Override
	public void addConnection(CacheHandler cache, INetworkListener connection) {
		IListReader reader = (IListReader) connection;
		if (reader.getValidHandlers().contains(handler)) {
			if (!readers.contains(reader) && readers.add(reader)) {
				onChannelsChanged();
				tickChannels();
			}
		}
	}

	@Override
	public void removeConnection(CacheHandler cache, INetworkListener connection) {
		if (connection instanceof IListReader && readers.remove(connection)) {
			removeReaderUsedChannels(((IListReader) connection).getIdentity());
			onChannelsChanged();
			tickChannels();
		}
	}

	@Override
	public void onChannelsChanged() {
		channels = handler.getAllChannels(new HashMap<>(), network);
		readers.forEach(this::updateReaderUsedChannels);
		tickChannels();
	}

	public void updateReaderUsedChannels(IListReader reader) {
		usedChannels.put(reader.getIdentity(), reader.getUsedChannels(channels));
	}

	public void removeReaderUsedChannels(int identity) {
		usedChannels.remove(identity);
	}

	public void updateChannels() {
		int used = 0;
		while (channelIterator.hasNext() && used != channelsPerTick) {
			Entry<NodeConnection, AbstractChangeableList<M>> entry = channelIterator.next();
			AbstractChangeableList<M> newList = handler.updateConnection(this, entry.getValue(), entry.getKey());
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
		for (Entry<NodeConnection, AbstractChangeableList<M>> entry : channels.entrySet()) {
			channels.put(entry.getKey(), handler.updateConnection(this, entry.getValue(), entry.getKey()));
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
			Pair<InfoUUID, AbstractChangeableList> list = handler.updateAndSendList(network, reader, channels, false);
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
