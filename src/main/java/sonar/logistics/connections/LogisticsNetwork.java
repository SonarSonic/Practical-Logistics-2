package sonar.logistics.connections;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.SonarHelper;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.network.PacketChannels;

public class LogisticsNetwork implements ILogisticsNetwork {

	public final ListenableList<ILogisticsNetwork> subNetworks = new ListenableList(this, 2);
	public final Map<Class, INetworkChannels> handlers = Maps.newLinkedHashMap();
	public final List<IInfoProvider> localMonitors = Lists.newArrayList();
	private List<NetworkUpdate> toUpdate = SonarHelper.convertArray(NetworkUpdate.values());
	private List<CacheHandler> changedCaches = Lists.newArrayList(CacheHandler.handlers);
	private Map<CacheHandler, List> caches = LogisticsHelper.getCachesMap();
	public Queue<INetworkListener> toAdd = new ConcurrentLinkedQueue<INetworkListener>();
	public Queue<INetworkListener> toRemove = new ConcurrentLinkedQueue<INetworkListener>();
	private List<NodeConnection> localChannels = Lists.newArrayList(), globalChannels = Lists.newArrayList(), allChannels = Lists.newArrayList();

	public int networkID;
	public boolean isValid = true;

	public LogisticsNetwork(int networkID) {
		this.networkID = networkID;
	}

	public void onNetworkCreated() {}

	public void onNetworkTick() {
		addConnections();
		removeConnections();
		updateCaches();
		runNetworkUpdates();
		updateNetworkHandlers();
	}

	@Override
	public void onNetworkRemoved() {
		isValid = false;

		List<ILogisticsNetwork> watching = subNetworks.getListeners(ILogisticsNetwork.WATCHING_NETWORK);
		watching.forEach(network -> network.getListenerList().removeListener(this, true, ILogisticsNetwork.CONNECTED_NETWORK));
		subNetworks.invalidateList();

		getCachedTiles(CacheHandler.TILE, CacheType.LOCAL).forEach(TILE -> CacheHandler.TILE.onConnectionRemoved(this, TILE));
		handlers.values().forEach(CHANNELS -> CHANNELS.onDeleted());

		caches.clear();
		handlers.clear();
	}

	public void onCablesChanged() {
		caches = LogisticsHelper.getCachesMap();
		PL2.getCableManager().getConnections(networkID).forEach(cable -> cable.addConnections(this));
		updateChannels();
	}

	/// UPDATE CACHES \\\
	public void onCacheChanged(CacheHandler...caches) {
		for (CacheHandler cache : caches)
			if (!changedCaches.contains(cache)) {
				changedCaches.add(cache);
			}
	}

	public void updateCaches() {
		if (!changedCaches.isEmpty()) {
			changedCaches.forEach(handler -> handler.update(this, caches.get(handler)));
			changedCaches.clear();
		}
	}

	@Override
	public void markUpdate(NetworkUpdate...updates) {
		for (NetworkUpdate update : updates)
			if (!toUpdate.contains(update)) {
				toUpdate.add(update);
			}
	}

	private void runNetworkUpdates() {
		if (!toUpdate.isEmpty()) {
			for (NetworkUpdate update : NetworkUpdate.values()) { // order is respected
				if (!toUpdate.contains(update)) {
					continue;
				}
				switch (update) {
				case GLOBAL:
					updateGlobalChannels();
					break;
				case HANDLER_CHANNELS:
					updateHandlerChannels();
					break;
				case LOCAL:
					updateLocalChannels();
					break;
				case SUB_NETWORKS:
					updateSubNetworks();
					break;
				}
			}
			toUpdate.clear();
		}
	}

	public boolean validateTile(INetworkListener listener) {
		if (!listener.isValid()) {// || listener.getNetworkID() != this.networkID) {
			removeConnection(listener);
			return false;
		}
		return true;
	}

	@Override
	public void onConnectionChanged(INetworkListener tile) {
		if (validateTile(tile)) {
			CacheHandler.getValidCaches(tile).forEach(cache -> {
				if (!changedCaches.contains(cache)) {
					changedCaches.add(cache);
				}
			});
		}
	}

	@Override
	public void addConnection(INetworkListener tile) {
		toAdd.add(tile);
		toRemove.remove(tile); // prevents tiles being removed if it's unnecessary
	}

	@Override
	public void removeConnection(INetworkListener tile) {
		toRemove.add(tile);
		toAdd.remove(tile); // prevents tiles being removed if it's unnecessary
	}

	public void addConnections() {
		if (toAdd.isEmpty())
			return;
		Iterator<INetworkListener> iterator = toAdd.iterator();
		while (iterator.hasNext()) {
			INetworkListener tile = iterator.next();
			CacheHandler.getValidCaches(tile).forEach(CACHE -> {
				if (!caches.get(CACHE).contains(tile) && caches.get(CACHE).add(tile)) {
					onCacheChanged(CACHE);
					CACHE.onConnectionAdded(this, tile);
				}
			});
			iterator.remove();
		}
	}

	public void removeConnections() {
		if (toRemove.isEmpty())
			return;
		Iterator<INetworkListener> iterator = toRemove.iterator();
		while (iterator.hasNext()) {
			INetworkListener tile = iterator.next();
			CacheHandler.getValidCaches(tile).forEach(CACHE -> {
				if (caches.get(CACHE).remove(tile)) {
					onCacheChanged(CACHE);
					CACHE.onConnectionRemoved(this, tile);
				}
			});
			iterator.remove();
		}
	}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {
		if (!localMonitors.contains(monitor))
			localMonitors.add(monitor);
	}

	@Override
	public void removeLocalInfoProvider(IInfoProvider monitor) {
		localMonitors.remove(monitor);
	}

	/// SUB NETWORKS \\\

	@Override
	public void onListenerAdded(ListenerTally<ILogisticsNetwork> tally) {
		if (tally.listener == this) {
			return;
		}
		tally.listener.getListenerList().addListener(this, ILogisticsNetwork.CONNECTED_NETWORK);
	}

	@Override
	public void onListenerRemoved(ListenerTally<ILogisticsNetwork> tally) {
		if (tally.listener == this) {
			return;
		}
		tally.listener.getListenerList().removeListener(this, true, ILogisticsNetwork.CONNECTED_NETWORK);
	}

	@Override
	public void onSubListenableAdded(ISonarListenable<ILogisticsNetwork> listen) {}

	@Override
	public void onSubListenableRemoved(ISonarListenable<ILogisticsNetwork> listen) {}

	@Override
	public ListenableList<ILogisticsNetwork> getListenerList() {
		return subNetworks;
	}

	@Override
	public void sendConnectionsPacket(EntityPlayer player) {
		MonitoredList<IInfo> coords = createConnectionsList(CacheType.ALL);
		NBTTagCompound channelTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), coords.isEmpty(), coords.copyInfo(), SyncType.DEFAULT_SYNC);
		if (channelTag.hasNoTags())
			return;
		PL2.network.sendTo(new PacketChannels(getNetworkID(), channelTag), (EntityPlayerMP) player);
	}

	/// CHANNELS \\\
	@Override
	public List<NodeConnection> getConnections(CacheType cacheType) {
		switch (cacheType) {
		case GLOBAL:
			return globalChannels;
		case LOCAL:
			return localChannels;
		default:
			return allChannels;
		}
	}

	private void updateSubNetworks() {
		subNetworks.invalidateList();
		subNetworks.validateList();
		getCachedTiles(CacheHandler.RECEIVERS, CacheType.LOCAL).forEach(r -> {
			if (validateTile(r)) {
				r.refreshConnectedNetworks();
				LogisticsHelper.addConnectedNetworks(this, r);
			}
		});
	}

	private void updateChannels() {
		updateLocalChannels();
		updateGlobalChannels();
	}

	private void updateLocalChannels() {
		List<NodeConnection> channels = Lists.newArrayList();
		LogisticsHelper.sortNodeConnections(channels, getCachedTiles(CacheHandler.NODES, CacheType.LOCAL));
		this.localChannels = Lists.newArrayList(channels);
	}

	private void updateGlobalChannels() {
		List<NodeConnection> channels = Lists.newArrayList();
		LogisticsHelper.sortNodeConnections(channels, getCachedTiles(CacheHandler.NODES, CacheType.GLOBAL));
		this.globalChannels = Lists.newArrayList(channels);

		List<NodeConnection> all = Lists.newArrayList();
		ListHelper.addWithCheck(all, globalChannels);
		ListHelper.addWithCheck(all, localChannels);
		NodeConnection.sortConnections(all);
		this.allChannels = all;
	}

	private void updateNetworkHandlers() {
		handlers.values().forEach(CHANNELS -> CHANNELS.updateChannel());
		for (IInfoProvider provider : localMonitors) {
			PacketHelper.sendNormalProviderInfo(provider);
		}
	}

	private void updateHandlerChannels() {
		handlers.forEach((H, C) -> C.onChannelsChanged());
	}

	public MonitoredList<IInfo> createConnectionsList(CacheType cacheType) {
		MonitoredList<IInfo> list = MonitoredList.<IInfo>newMonitoredList(getNetworkID());
		getConnections(cacheType).forEach(CHANNEL -> list.add(CHANNEL.getChannel()));
		return list;
	}

	public <T> List<T> getCachedTiles(CacheHandler<T> handler, CacheType cacheType) {
		List<T> tiles = cacheType.isLocal() ? Lists.newArrayList(caches.getOrDefault(handler, Lists.newArrayList())) : Lists.newArrayList();
		if (cacheType.isGlobal()) {
			List<ILogisticsNetwork> connected = subNetworks.getListeners(ILogisticsNetwork.CONNECTED_NETWORK);
			connected.forEach(network -> ListHelper.addWithCheck(tiles, network.getCachedTiles(handler, CacheType.LOCAL)));
		}
		return tiles;
	}

	@Override
	public <T extends INetworkChannels> T getNetworkChannels(Class<T> channelClass) {
		return (T) handlers.get(channelClass);
	}

	public <T extends INetworkChannels> T getOrCreateNetworkChannels(Class<T> channelClass) {
		return (T) handlers.computeIfAbsent(channelClass, c -> LogisticsHelper.getChannelInstance(channelClass, this));
	}

	@Override
	public List<IInfoProvider> getLocalInfoProviders() {
		return localMonitors;
	}

	@Override
	public IInfoProvider getLocalInfoProvider() {
		return !localMonitors.isEmpty() ? localMonitors.get(0) : null;
	}

	@Override
	public int getNetworkID() {
		return networkID;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

}