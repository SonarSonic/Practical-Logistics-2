package sonar.logistics.api.connecting;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.CacheType;

public class EmptyNetworkCache implements ILogisticsNetwork  {

	public static final EmptyNetworkCache INSTANCE = new EmptyNetworkCache();

	@Override
	public void addConnection(INetworkListener tile) {}

	@Override
	public void removeConnection(INetworkListener tile) {}

	@Override
	public void onConnectionChanged(INetworkListener tile) {}


	@Override
	public void onNetworkRemoved() {}

	@Override
	public int getNetworkID() {
		return -1;
	}

	@Override
	public <T> ArrayList<T> getConnections(CacheHandler<T> handler, CacheType cacheType) {
		return Lists.newArrayList();
	}
	
	@Override
	public ArrayList<NodeConnection> getChannels(CacheType cacheType) {
		return Lists.newArrayList();
	}

	@Override
	public void onNetworkTick() {		
	}

	@Override
	public boolean isFakeNetwork() {
		return true;
	}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {}

	@Override
	public void removeLocalInfoProvider(IInfoProvider monitor) {}

	@Override
	public IInfoProvider getLocalInfoProvider() {
		return null;
	}

	@Override
	public ArrayList<IInfoProvider> getLocalInfoProviders() {
		return Lists.newArrayList();
	}

	@Override
	public void markCacheDirty(CacheHandler cache) {}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public ListenerList<ILogisticsNetwork> getListenerList() {
		return null;
	}

	@Override
	public void onListenerAdded(ListenerTally<ILogisticsNetwork> tally) {}

	@Override
	public void onListenerRemoved(ListenerTally<ILogisticsNetwork> tally) {}
}
