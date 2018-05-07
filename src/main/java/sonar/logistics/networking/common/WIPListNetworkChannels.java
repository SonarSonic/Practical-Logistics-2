package sonar.logistics.networking.common;

/**an alternative versions which does sub lists properly, i.e. preventing blocks being read twice. had a lot of trouble for minimal performance increase...may revisit later*/
/*
public class WIPListNetworkChannels<M extends IMonitorInfo, H extends INetworkListHandler<M>> extends DefaultNetworkChannels<H> implements INetworkListChannels<H> {

	private List<IListReader<M>> readers = new ArrayList<>();
	private Iterator<IListReader<M>> readerIterator;
	private int readersPerTick = 0;

	private List<INetworkListChannels<H>> subChannels; // networking which take connections from this one
	private List<INetworkListChannels<H>> masterChannels; // networking which give connections to this one

	private Map<NodeConnection, MonitoredList<M>> globalInfo = new HashMap<>();
	private Map<NodeConnection, MonitoredList<M>> localInfo = new HashMap<>();
	private Map<NodeConnection, MonitoredList<M>> fullInfo = new HashMap<>();
	private Iterator<Entry<NodeConnection, MonitoredList<M>>> channelIterator;
	private ChannelList currentList;
	private int channelsPerTick = 0;

	public boolean hasListeners = false;

	public WIPListNetworkChannels(H handler, ILogisticsNetwork network) {
		super(handler, network, CacheHandler.READER);
	}

	@Override
	public void onCreated() {
		checkAndLinkChannels();
	}

	@Override
	public void onDeleted() {
		super.onDeleted();
		if (subChannels != null && !subChannels.isEmpty()) {
			Lists.newArrayList(subChannels).forEach(master -> master.removeMasterChannels(this));
		}
		masterChannels.clear();
		subChannels.clear();
		readers.clear();
		readerIterator = null;
		localInfo.clear();
		channelIterator = null;
		currentList = null;
	}

	public void checkAndLinkChannels() {
		network.getListenerList().getListeners(ILogisticsNetwork.WATCHING_NETWORK).forEach(network -> {
			INetworkChannels channels = network.getNetworkChannels(handler);
			if (channels != null && channels.getNetwork().isValid()) {
				((INetworkListChannels<H>) channels).addMasterChannels(this);
			}
		});
		network.getListenerList().getListeners(ILogisticsNetwork.CONNECTED_NETWORK).forEach(network -> {
			INetworkChannels channels = network.getNetworkChannels(handler);
			if (channels != null && channels.getNetwork().isValid()) {
				addMasterChannels((INetworkListChannels<H>) channels);
			}
		});
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
			this.channelsPerTick = localInfo.size() > handler.updateRate() ? (int) Math.ceil(localInfo.size() / Math.max(1, handler.updateRate())) : 1;
			this.channelIterator = localInfo.entrySet().iterator();
			this.readerIterator = readers.iterator();

			fullInfo = new HashMap<>();
			fullInfo.putAll(localInfo);
			fullInfo.putAll(globalInfo);
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
		checkAndLinkChannels();
		localInfo = handler.getAllChannels(new HashMap<>(), network);
		updateTicks();
	}

	private void updateChannels() {
		int used = 0;
		while (channelIterator.hasNext() && used != channelsPerTick) {
			Entry<NodeConnection, MonitoredList<M>> entry = channelIterator.next();
			MonitoredList<M> oldList = entry.getValue() == null ? MonitoredList.<M>newMonitoredList(network.getNetworkID()) : entry.getValue();
			MonitoredList<M> newList = handler.updateConnection(MonitoredList.<M>newMonitoredList(network.getNetworkID()), oldList, entry.getKey(), currentList);
			if (subChannels != null && !subChannels.isEmpty()) {
				subChannels.forEach(sub -> sub.onListUpdate(this, entry.getKey(), newList, false));
			}
			entry.setValue(newList);
			used++;
		}
	}

	private void updateReaders() {
		int used = 0;
		while (readerIterator.hasNext() && used != readersPerTick) {
			IListReader<M> reader = readerIterator.next();
			if (reader.getListenerList().hasListeners()) {
				handler.updateAndSendList(network, reader, fullInfo, true);
			}
			used++;
		}
	}

	public void updateAllChannels(boolean updateMasters) {
		for (Entry<NodeConnection, MonitoredList<M>> entry : localInfo.entrySet()) {
			MonitoredList<M> oldList = entry.getValue() == null ? MonitoredList.<M>newMonitoredList(network.getNetworkID()) : entry.getValue();
			MonitoredList<M> newList = handler.updateConnection(MonitoredList.<M>newMonitoredList(network.getNetworkID()), oldList, entry.getKey(), currentList);
			if (subChannels != null && !subChannels.isEmpty()) {
				subChannels.forEach(sub -> sub.onListUpdate(this, entry.getKey(), newList, true));
			}
			entry.setValue(newList);
		}
		if (updateMasters) {
			if (masterChannels != null && !masterChannels.isEmpty()) {
				masterChannels.forEach(master -> master.updateAllChannels(false));
			}
		}
	}

	private void updateAllReaders(boolean send) {
		readers.forEach(reader -> handler.updateAndSendList(network, reader, fullInfo, send));
	}

	public void sendFullRapidUpdate() {
		updateAllChannels(true);
		updateAllReaders(true);
	}

	public void sendLocalRapidUpdate(IListReader<M> reader, EntityPlayer viewer) {
		PlayerListener listener = reader.getListenerList().findListener(viewer);
		if (listener != null) {
			updateAllChannels(true);
			Pair<InfoUUID, MonitoredList<M>> list = handler.updateAndSendList(network, reader, fullInfo, false);
			LogisticsHelper.sendFullInfo(Lists.newArrayList(listener), reader, list.b, list.a);
		}
	}

	@Override
	public void onListUpdate(INetworkListChannels<H> source, NodeConnection node, MonitoredList list, boolean rapid) {
		if (rapid) {
			fullInfo.put(node, list);
		} else {
			globalInfo.put(node, list);
		}
	}

	@Override
	public void addSubChannels(INetworkListChannels<H> channels) {
		if (subChannels == null)
			subChannels = new ArrayList<>();
		if (!subChannels.contains(channels))
			subChannels.add(channels);
	}

	@Override
	public void removeSubChannels(INetworkListChannels<H> channels) {
		subChannels.remove(channels);
	}

	@Override
	public void addMasterChannels(INetworkListChannels<H> channels) {
		if (masterChannels == null)
			masterChannels = new ArrayList<>();
		if (!masterChannels.contains(channels)) {
			masterChannels.add(channels);
			channels.addSubChannels(this);
		}
	}

	@Override
	public void removeMasterChannels(INetworkListChannels<H> channels) {
		if (masterChannels.remove(channels)) {
			channels.removeSubChannels(this);
			globalInfo.clear();
		}
	}
}
*/