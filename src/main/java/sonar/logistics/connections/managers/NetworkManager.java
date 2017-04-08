package sonar.logistics.connections.managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.Lists;

import sonar.logistics.PL2;
import sonar.logistics.PL2Config;
import sonar.logistics.api.connecting.EmptyNetworkCache;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.connecting.INetworkListener;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.CacheType;
import sonar.logistics.connections.LogisticsNetwork;
import sonar.logistics.connections.monitoring.ChannelMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;

public class NetworkManager {

	public Map<Integer, ILogisticsNetwork> cache = new ConcurrentHashMap<Integer, ILogisticsNetwork>();
	public Map<Integer, MonitoredList<IMonitorInfo>> channelMap = new ConcurrentHashMap<Integer, MonitoredList<IMonitorInfo>>();
	public ChannelMonitorHandler handler = new ChannelMonitorHandler();

	public void removeAll() {
		cache.clear();
		channelMap.clear();
	}

	public void tick() {
		if (cache.isEmpty()) {
			return;
		}
		Set<Entry<Integer, ILogisticsNetwork>> entrySet = cache.entrySet();
		for (final Iterator<Entry<Integer, ILogisticsNetwork>> iterator = entrySet.iterator(); iterator.hasNext();) {
			Entry<Integer, ILogisticsNetwork> entry = iterator.next();
			ILogisticsNetwork network = entry.getValue(); // TODO does it need to update networkID
			if (network.isFakeNetwork() || PL2.instance.cableManager.getConnections(network.getNetworkID()).size() == 0) {
				if (network.isValid()) {
					network.onNetworkRemoved();
				}
				iterator.remove();
			} else {
				network.onNetworkTick();
			}
		}
	}

	public ArrayList<NodeConnection> getChannelArray(int networkID) {
		ILogisticsNetwork network = getNetwork(networkID);
		return network != null ? network.getChannels(CacheType.GLOBAL) : Lists.newArrayList();
	}

	public ILogisticsNetwork getNetwork(int networkID) {
		ILogisticsNetwork networkCache = cache.get(networkID);
		return networkCache != null ? networkCache : EmptyNetworkCache.INSTANCE;
	}

	public ILogisticsNetwork getOrCreateNetwork(int networkID) {
		ILogisticsNetwork networkCache = cache.get(networkID);
		if (networkCache == null || networkCache.isFakeNetwork()) {
			LogisticsNetwork network = new LogisticsNetwork(networkID);// TODO int arg
			network.updateTicks = ThreadLocalRandom.current().nextInt(0, PL2Config.updateRate + 1); // to prevents every network ticking at the same time
			cache.put(networkID, network);
			networkCache = cache.get(networkID);
		}
		return networkCache;
	}

	/* public void markNetworkDirty(int networkID, RefreshType type) { ILogisticsNetwork2 cache = getOrCreateNetwork(networkID); cache.markDirty(type); } */
	public void connectNetworks(int oldID, int newID) {
		if (oldID != newID) {
			ILogisticsNetwork oldNet = cache.get(oldID);
			if (oldNet == null) {
				return;
			}
			ILogisticsNetwork newNet = getOrCreateNetwork(newID);

			ArrayList<INetworkListener> tiles = oldNet.getConnections(CacheHandler.TILE, CacheType.LOCAL);
			oldNet.onNetworkRemoved();
			tiles.forEach(tile -> newNet.addConnection(tile));
			cache.remove(oldNet);

		}
	}

	public Map<Integer, ILogisticsNetwork> getNetworkCache() {
		return cache;
	}

	public Map<Integer, MonitoredList<IMonitorInfo>> getCoordMap() {
		return channelMap;
	}
}
