package sonar.logistics.networking;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sonar.logistics.api.networks.EmptyLogisticsNetwork;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.utils.CacheType;

public class LogisticsNetworkHandler {

	
	
	public Map<Integer, ILogisticsNetwork> cache = new ConcurrentHashMap<Integer, ILogisticsNetwork>();

	public void removeAll() {
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

	/**will never be null*/
	public ILogisticsNetwork getNetwork(int networkID) {
		ILogisticsNetwork networkCache = cache.get(networkID);
		return networkCache != null ? networkCache : EmptyLogisticsNetwork.INSTANCE;
	}

	public ILogisticsNetwork getOrCreateNetwork(int networkID) {
		ILogisticsNetwork networkCache = cache.get(networkID);
		if (networkCache == null || !networkCache.isValid()) {
			LogisticsNetwork network = new LogisticsNetwork(networkID);// TODO int arg
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
			List<INetworkListener> tiles = oldNet.getCachedTiles(CacheHandler.TILE, CacheType.LOCAL);
			oldNet.onNetworkRemoved();
			cache.remove(oldNet);				
			newNet.markUpdate(NetworkUpdate.CABLES);
			newNet.onCablesChanged();
		}
	}

	public Map<Integer, ILogisticsNetwork> getNetworkCache() {
		return cache;
	}
}
