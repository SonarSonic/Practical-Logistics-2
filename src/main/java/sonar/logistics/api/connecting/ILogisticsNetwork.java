package sonar.logistics.api.connecting;

import java.util.ArrayList;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.core.listener.ListenerList;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.CacheType;

public interface ILogisticsNetwork extends ISonarListener, ISonarListenable<ILogisticsNetwork> {

	public static final int CONNECTED_NETWORK = 0;
	public static final int WATCHING_NETWORK = 1;

	public <T> ArrayList<T> getConnections(CacheHandler<T> handler, CacheType cacheType);

	public ArrayList<NodeConnection> getChannels(CacheType cacheType);

	public void addConnection(INetworkListener tile);

	public void removeConnection(INetworkListener tile);

	/** this is the only method a connection should ever call itself!! */
	public void onConnectionChanged(INetworkListener tile);

	public void markCacheDirty(CacheHandler cache);

	public void onNetworkRemoved();

	public void onNetworkTick();

	public int getNetworkID();

	public boolean isFakeNetwork();

	public void addLocalInfoProvider(IInfoProvider monitor);

	public void removeLocalInfoProvider(IInfoProvider monitor);

	public IInfoProvider getLocalInfoProvider();

	public ArrayList<IInfoProvider> getLocalInfoProviders();

}
