package sonar.logistics.api.networks;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.logistics.api.info.IInfo;
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
	
	int getNetworkID();
	
	void onNetworkCreated();
	
	void onNetworkTick();	
	
	void onNetworkRemoved();
	
	void onCablesChanged();

	void onCacheChanged(CacheHandler... cache);
	
	void markUpdate(NetworkUpdate... updates);
	
	boolean validateTile(INetworkListener listener);

	/** this is the only method a connection should ever call itself!! */
	void onConnectionChanged(INetworkListener tile);

	void addConnection(INetworkListener tile);

	void removeConnection(INetworkListener tile);
	
	void addConnections();
	
	void removeConnections();

	void addLocalInfoProvider(IInfoProvider monitor);

	void removeLocalInfoProvider(IInfoProvider monitor);	
	
	void sendChannelPacket(EntityPlayer player);

	List<NodeConnection> getChannels(CacheType cacheType);	
	
	MonitoredList<IInfo> createChannelList(CacheType cacheType);
	
	<T> List<T> getConnections(CacheHandler<T> handler, CacheType cacheType);
	
	@Nullable <H extends INetworkHandler> INetworkChannels getNetworkChannels(H handler);
	
	List<IInfoProvider> getLocalInfoProviders();	

	IInfoProvider getLocalInfoProvider();


}