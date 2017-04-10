package sonar.logistics.api.networks;

import java.util.List;

import javax.annotation.Nullable;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.NetworkUpdate;

public interface ILogisticsNetwork extends ISonarListener, ISonarListenable<ILogisticsNetwork> {

	/**networks which provide connects to this one*/
	public static final int CONNECTED_NETWORK = 0;
	
	/**networks which take info from this one*/
	public static final int WATCHING_NETWORK = 1;
	
	public int getNetworkID();
	
	public void onNetworkCreated();
	
	public void onNetworkTick();	
	
	public void onNetworkRemoved();
	
	public void onCablesChanged();

	public void onCacheChanged(CacheHandler... cache);
	
	public void markUpdate(NetworkUpdate... updates);
	
	public boolean validateTile(INetworkListener listener);

	/** this is the only method a connection should ever call itself!! */
	public void onConnectionChanged(INetworkListener tile);

	public void addConnection(INetworkListener tile);

	public void removeConnection(INetworkListener tile);
	
	public void addConnections();
	
	public void removeConnections();

	public void addLocalInfoProvider(IInfoProvider monitor);

	public void removeLocalInfoProvider(IInfoProvider monitor);	

	public List<NodeConnection> getChannels(CacheType cacheType);	
	
	public MonitoredList<IMonitorInfo> createChannelList(CacheType cacheType);
	
	public <T> List<T> getConnections(CacheHandler<T> handler, CacheType cacheType);
	
	public @Nullable <H extends INetworkHandler> INetworkChannels getNetworkChannels(H handler);
	
	public List<IInfoProvider> getLocalInfoProviders();	

	public IInfoProvider getLocalInfoProvider();


}