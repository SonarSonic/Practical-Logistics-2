package sonar.logistics.core.tiles.connections.data.network;

import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.base.events.LogisticsEventHandler;
import sonar.logistics.base.events.NetworkChanges;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.CacheType;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LogisticsNetworkHandler {

	public static LogisticsNetworkHandler instance() {
		return PL2.proxy.networkManager;
	}
	
	public Map<Integer, ILogisticsNetwork> cache = new ConcurrentHashMap<>();
	private int ID_COUNT;

	public void removeAll() {
		ID_COUNT = 0;
		cache.clear();
	}

	public void tick() {
		if (cache.isEmpty()) {
			return;
		}
		Set<Entry<Integer, ILogisticsNetwork>> entrySet = cache.entrySet();
		for (final Iterator<Entry<Integer, ILogisticsNetwork>> iterator = entrySet.iterator(); iterator.hasNext();) {
			Entry<Integer, ILogisticsNetwork> entry = iterator.next();
			ILogisticsNetwork network = entry.getValue(); // TODO does it need to update networkID
			if (!network.isValid()) {			
				iterator.remove();
			} else {
				network.onNetworkTick();
			}
		}
	}

	public int getNextIdentity(){
		return ID_COUNT++;
	}

	public int getCurrentIdentity() {
		return ID_COUNT;
	}

	/** warning do not use unless reading WorldSavedData, or your world will be corrupted!!!! */
	public int setIdentityCount(int count) {
		return ID_COUNT = count;
	}

	@Nonnull
	public ILogisticsNetwork getNetwork(int networkID) {
		ILogisticsNetwork networkCache = cache.get(networkID);
		return networkCache != null ? networkCache : EmptyLogisticsNetwork.INSTANCE;
	}

	public ILogisticsNetwork getOrCreateNetwork(int networkID) {
		if(networkID==-1){
			return EmptyLogisticsNetwork.INSTANCE;
		}
		ILogisticsNetwork networkCache = cache.get(networkID);
		if (networkCache == null || !networkCache.isValid()) {
			LogisticsNetwork network = new LogisticsNetwork(networkID);// TODO int arg
			network.onNetworkCreated();
			cache.put(networkID, network);
			networkCache = cache.get(networkID);
		}
		return networkCache;
	}

	public void connectNetworks(int oldID, int newID) {
		if (oldID != newID) {
			ILogisticsNetwork oldNet = cache.get(oldID);
			if (oldNet == null) {
				return;
			}
			ILogisticsNetwork newNet = getOrCreateNetwork(newID);
			List<INetworkTile> tiles = oldNet.getCachedTiles(CacheHandler.TILE, CacheType.LOCAL);
			oldNet.onNetworkRemoved();
			cache.remove(oldID);

			LogisticsEventHandler.instance().queueNetworkChange(newNet, NetworkChanges.LOCAL_CHANNELS, NetworkChanges.LOCAL_PROVIDERS);
		}
	}

	public Map<Integer, ILogisticsNetwork> getNetworkCache() {
		return cache;
	}
}
