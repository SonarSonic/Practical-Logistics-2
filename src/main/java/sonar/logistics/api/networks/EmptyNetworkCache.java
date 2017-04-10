package sonar.logistics.api.networks;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.NetworkUpdate;

/**the default version of ILogisticsNetwork when a tile is yet to be connected*/
public class EmptyNetworkCache implements ILogisticsNetwork  {

	public static final EmptyNetworkCache INSTANCE = new EmptyNetworkCache();
	
	@Override
	public void onNetworkCreated() {}

	@Override
	public void onNetworkTick() {}

	@Override
	public void onNetworkRemoved() {}

	@Override
	public void onCablesChanged() {}

	@Override
	public void onCacheChanged(CacheHandler ...cache) {}

	@Override
	public void markUpdate(NetworkUpdate ...updates) {}

	@Override
	public boolean validateTile(INetworkListener listener) {
		return false;
	}

	@Override
	public void onConnectionChanged(INetworkListener tile) {}

	@Override
	public void addConnection(INetworkListener tile) {}

	@Override
	public void removeConnection(INetworkListener tile) {}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {}

	@Override
	public void addConnections(){}

	@Override
	public void removeConnections(){}

	@Override
	public void removeLocalInfoProvider(IInfoProvider monitor) {}

	@Override
	public void onListenerAdded(ListenerTally<ILogisticsNetwork> tally) {}

	@Override
	public void onListenerRemoved(ListenerTally<ILogisticsNetwork> tally) {}

	@Override
	public void onSubListenableAdded(ISonarListenable<ILogisticsNetwork> listen) {}

	@Override
	public void onSubListenableRemoved(ISonarListenable<ILogisticsNetwork> listen) {}


	@Override
	public ListenableList<ILogisticsNetwork> getListenerList() {
		return null;
	}
	
	@Override
	public List<NodeConnection> getChannels(CacheType cacheType) {
		return Lists.newArrayList();
	}

	@Override
	public MonitoredList<IMonitorInfo> createChannelList(CacheType cacheType) {
		return MonitoredList.newMonitoredList(-1);
	}

	@Override
	public <T> List<T> getConnections(CacheHandler<T> handler, CacheType cacheType) {
		return Lists.newArrayList();
	}

	@Override
	public <H extends INetworkHandler> INetworkChannels getNetworkChannels(H handler) {
		return null;
	}

	@Override
	public List<IInfoProvider> getLocalInfoProviders() {
		return Lists.newArrayList();
	}

	@Override
	public IInfoProvider getLocalInfoProvider() {
		return null;
	}
	
	@Override
	public boolean isValid() {
		return false;
	}
	
	@Override
	public int getNetworkID() {
		return 0;
	}
}
