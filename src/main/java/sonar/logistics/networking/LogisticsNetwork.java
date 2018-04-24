package sonar.logistics.networking;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ListenableList;
import sonar.logistics.PL2;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.networking.displays.LocalProviderHandler;
import sonar.logistics.networking.displays.LocalProviderHandler.UpdateCause;
import sonar.logistics.networking.info.InfoHelper;
import sonar.logistics.packets.PacketChannels;

public class LogisticsNetwork implements ILogisticsNetwork {

	public final ListenableList<ILogisticsNetwork> subNetworks = new ListenableList(this, 2);
	public final Map<Class, INetworkChannels> handlers = new LinkedHashMap<>();
	public List<IInfoProvider> localProviders = new ArrayList<>();
	public List<IInfoProvider> globalProviders = new ArrayList<>();
	private List<CacheHandler> changedCaches = Lists.newArrayList(CacheHandler.handlers);
	private Map<CacheHandler, List> caches = LogisticsHelper.getCachesMap();
	private List<NodeConnection> localChannels = new ArrayList<>(), globalChannels = new ArrayList<>(), allChannels = new ArrayList<>();
	private long tickStart = 0;
	private long updateTick = 0; // in nano seconds

	public int networkID;
	public boolean isValid = true;

	public LogisticsNetwork(int networkID) {
		this.networkID = networkID;
	}

	//// NETWORK EVENTS \\\\

	public void onNetworkCreated() {
		
	}

	public void onNetworkTick() {
		tickStart = System.nanoTime();
		updateCaches();
		updateNetworkHandlers();
		updateTick = System.nanoTime() - tickStart;
	}

	@Override
	public void onNetworkRemoved() {
		isValid = false;

		List<ILogisticsNetwork> watching = subNetworks.getListeners(ILogisticsNetwork.WATCHING_NETWORK);
		List<ILogisticsNetwork> connected = subNetworks.getListeners(ILogisticsNetwork.CONNECTED_NETWORK);
		watching.forEach(network -> {
			network.getListenerList().removeListener(this, true, ILogisticsNetwork.CONNECTED_NETWORK);
			network.onCacheChanged(CacheHandler.RECEIVERS);
		});
		connected.forEach(network -> {
			network.getListenerList().removeListener(this, true, ILogisticsNetwork.WATCHING_NETWORK);
			network.onCacheChanged(CacheHandler.EMITTERS);
		});
		subNetworks.invalidateList();
		caches.forEach((cache_handler, cache_list) -> {
			cache_list.forEach(tile -> cache_handler.onConnectionRemoved(this, tile)); // removing on every
		});

		handlers.values().forEach(INetworkChannels::onDeleted);

		caches.clear();
		handlers.clear();

	}

	//// UPDATE CACHES \\\\

	@Override
	public void onConnectionChanged(INetworkListener tile) {
		if (validateTile(tile)) {
			ListHelper.addWithCheck(changedCaches, CacheHandler.getValidCaches(tile));
		}
	}

	public void onCacheChanged(CacheHandler... caches) {
		ListHelper.addWithCheck(changedCaches, caches);
	}

	public void updateCaches() {
		if (!changedCaches.isEmpty()) {
			changedCaches.forEach(handler -> handler.update(this, caches.get(handler)));
			changedCaches.clear();
		}
	}

	public InfoChangeableList<MonitoredBlockCoords> createConnectionsList(CacheType cacheType) {
		InfoChangeableList list = new InfoChangeableList();
		getConnections(cacheType).forEach(CHANNEL -> list.add(CHANNEL.getChannel()));
		return list;
	}

	public <T> List<T> getCachedTiles(CacheHandler<T> handler, CacheType cacheType) {
		List<T> tiles = cacheType.isLocal() ? Lists.newArrayList(caches.getOrDefault(handler, new ArrayList<>())) : new ArrayList<>();
		if (cacheType.isGlobal()) {
			List<ILogisticsNetwork> connected = NetworkHelper.getAllNetworks(this, ILogisticsNetwork.CONNECTED_NETWORK);
			connected.forEach(network -> ListHelper.addWithCheck(tiles, network.getCachedTiles(handler, CacheType.LOCAL)));
		}
		return tiles;
	}

	public boolean validateTile(INetworkListener listener) {
		if (!listener.isValid()) {
			removeConnection(listener);
			return false;
		}
		return true;
	}

	@Override
	public void addConnection(INetworkListener tile) {
		CacheHandler.getValidCaches(tile).forEach(cache -> {
			if (!caches.get(cache).contains(tile) && caches.get(cache).add(tile)) {
				onCacheChanged(cache);
				cache.onConnectionAdded(this, tile);
			}
		});
	}

	@Override
	public void removeConnection(INetworkListener tile) {
		CacheHandler.getValidCaches(tile).forEach(cache -> {
			if (caches.get(cache).remove(tile)) {
				onCacheChanged(cache);
				cache.onConnectionRemoved(this, tile);
			}
		});
	}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {
		if (ListHelper.addWithCheck(localProviders, monitor)) {
			LocalProviderHandler.queueUpdate(monitor, UpdateCause.NETWORK_CHANGE);
		}
	}

	@Override
	public void removeLocalInfoProvider(IInfoProvider monitor) {
		if (localProviders.remove(monitor)) {
			LocalProviderHandler.queueUpdate(monitor, UpdateCause.NETWORK_CHANGE);
		}
	}

	public void onCablesChanged() {
		// LogisticsEventHandler.instance().queueNetworkUpdate(this, NetworkUpdate.CABLES);
	}

	@Override
	public void sendConnectionsPacket(EntityPlayer player) {
		InfoChangeableList coords = createConnectionsList(CacheType.ALL);
		NBTTagCompound channelTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), coords, SyncType.DEFAULT_SYNC);
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

	// FIXME - make this event driven.
	public void createLocalChannels() {
		List<NodeConnection> channels = new ArrayList<>();
		LogisticsHelper.sortNodeConnections(channels, getCachedTiles(CacheHandler.NODES, CacheType.LOCAL));
		this.localChannels = Lists.newArrayList(channels);
	}

	public void createGlobalChannels() {
		List<NodeConnection> channels = new ArrayList<>();
		LogisticsHelper.sortNodeConnections(channels, getCachedTiles(CacheHandler.NODES, CacheType.GLOBAL));
		this.globalChannels = Lists.newArrayList(channels);

		List<NodeConnection> all = new ArrayList<>();
		ListHelper.addWithCheck(all, globalChannels);
		ListHelper.addWithCheck(all, localChannels);
		NodeConnection.sortConnections(all);
		this.allChannels = all;
		updateHandlerChannels();
	}

	public void createGlobalProviders() {
		this.globalProviders = Lists.newArrayList(localProviders);
		NetworkHelper.getAllNetworks(this, ILogisticsNetwork.CONNECTED_NETWORK).forEach(network -> ListHelper.addWithCheck(globalProviders, network.getLocalInfoProviders()));
	}

	public void updateNetworkHandlers() {
		handlers.values().forEach(INetworkChannels::updateChannel);
		localProviders.forEach(PacketHelper::sendNormalProviderInfo);
	}

	public void updateHandlerChannels() {
		handlers.forEach((H, C) -> C.onChannelsChanged());
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
		return localProviders;
	}

	@Override
	public List<IInfoProvider> getGlobalInfoProviders() {
		return globalProviders;
	}

	@Override
	public IInfoProvider getLocalInfoProvider() {
		return !localProviders.isEmpty() ? localProviders.get(0) : null;
	}

	@Override
	public int getNetworkID() {
		return networkID;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	/// SUB NETWORKS \\\

	public void notifyWatchingNetworks() {
		///getAllNetworks(ILogisticsNetwork.WATCHING_NETWORK).forEach(network -> network.onConnectedNetworkCacheChanged(this));
	}

	@Override
	public void onConnectedNetworkCacheChanged(ILogisticsNetwork network) {
		onCacheChanged(CacheHandler.RECEIVERS);
	}

	@Override
	public ListenableList<ILogisticsNetwork> getListenerList() {
		return subNetworks;
	}

	//// MONITORING \\\\

	public long getNetworkTickTime() {
		return updateTick;
	}
}