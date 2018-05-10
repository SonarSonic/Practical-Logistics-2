package sonar.logistics.core.tiles.connections.data.network;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ListenableList;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.nodes.IEntityNode;
import sonar.logistics.api.core.tiles.nodes.INode;
import sonar.logistics.api.core.tiles.readers.IInfoProvider;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.channels.INetworkChannels;
import sonar.logistics.api.core.tiles.wireless.emitters.IDataEmitter;
import sonar.logistics.api.core.tiles.wireless.receivers.IDataReceiver;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.channels.PacketChannels;
import sonar.logistics.base.filters.ITransferFilteredTile;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.base.utils.LogisticsHelper;
import sonar.logistics.core.tiles.displays.DisplayInfoReferenceHandler;
import sonar.logistics.core.tiles.displays.DisplayInfoReferenceHandler.UpdateCause;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;
import sonar.logistics.core.tiles.readers.info.handling.InfoHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LogisticsNetwork implements ILogisticsNetwork {


	public static final ArrayList<Class> CACHE_TYPES = Lists.newArrayList(IDataReceiver.class, IDataEmitter.class, IListReader.class, INetworkTile.class, INode.class, IEntityNode.class, ITransferFilteredTile.class);
	public final ListenableList<ILogisticsNetwork> subNetworks = new ListenableList(this, 2);
	public final Map<Class, INetworkChannels> handlers = new LinkedHashMap<>();
	public List<IInfoProvider> localProviders = new ArrayList<>();
	public List<IInfoProvider> globalProviders = new ArrayList<>();
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

	public void onNetworkCreated() {}

	public void onNetworkTick() {
		tickStart = System.nanoTime();
		updateNetworkHandlers();
		updateTick = System.nanoTime() - tickStart;
	}

	@Override
	public void onNetworkRemoved() {
		isValid = false;

		List<ILogisticsNetwork> watching = subNetworks.getListeners(ILogisticsNetwork.WATCHING_NETWORK);
		List<ILogisticsNetwork> connected = subNetworks.getListeners(ILogisticsNetwork.CONNECTED_NETWORK);
		watching.forEach(network -> network.getListenerList().removeListener(this, true, ILogisticsNetwork.CONNECTED_NETWORK));
		connected.forEach(network -> network.getListenerList().removeListener(this, true, ILogisticsNetwork.WATCHING_NETWORK));
		subNetworks.invalidateList();
		handlers.values().forEach(INetworkChannels::onDeleted);
		caches.clear();
		handlers.clear();
	}

	//// UPDATE CACHES \\\\

	public InfoChangeableList<MonitoredBlockCoords> createConnectionsList(CacheType cacheType) {
		InfoChangeableList list = new InfoChangeableList();
		getConnections(cacheType).forEach(CHANNEL -> list.add(CHANNEL.getChannel()));
		return list;
	}

	public <T extends INetworkTile> List<T> getCachedTiles(CacheHandler<T> handler, CacheType cacheType) {
		List<T> tiles = cacheType.isLocal() ? Lists.newArrayList(caches.getOrDefault(handler, new ArrayList<>())) : new ArrayList<>();
		if (cacheType.isGlobal()) {
			List<ILogisticsNetwork> connected = NetworkHelper.getAllNetworks(this, ILogisticsNetwork.CONNECTED_NETWORK);
			connected.forEach(network -> ListHelper.addWithCheck(tiles, network.getCachedTiles(handler, CacheType.LOCAL)));
		}
		return tiles;
	}

	public boolean validateTile(INetworkTile listener) {
		if (!listener.isValid()) {
			removeConnection(listener);
			return false;
		}
		return true;
	}

	@Override
	public void addConnection(INetworkTile tile) {
		CacheHandler.getValidCaches(tile).forEach(cache -> ListHelper.addWithCheck(caches.get(cache), tile));
	}

	@Override
	public void removeConnection(INetworkTile tile) {
		CacheHandler.getValidCaches(tile).forEach(cache -> caches.get(cache).remove(tile));
	}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {
		if (ListHelper.addWithCheck(localProviders, monitor)) {
			DisplayInfoReferenceHandler.queueUpdate(monitor, UpdateCause.NETWORK_CHANGE);
		}
	}

	@Override
	public void removeLocalInfoProvider(IInfoProvider monitor) {
		if (localProviders.remove(monitor)) {
			DisplayInfoReferenceHandler.queueUpdate(monitor, UpdateCause.NETWORK_CHANGE);
		}
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
		localProviders.forEach(InfoPacketHelper::sendNormalProviderInfo);
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
	public int getNetworkID() {
		return networkID;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Override
	public ListenableList<ILogisticsNetwork> getListenerList() {
		return subNetworks;
	}

	@Override
	public long getNetworkTickTime() {
		return updateTick;
	}
}