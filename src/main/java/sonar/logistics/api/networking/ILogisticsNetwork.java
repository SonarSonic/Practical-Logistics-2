package sonar.logistics.api.networking;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.networking.CacheHandler;

import javax.annotation.Nullable;
import java.util.List;

/**the default implementation of a Logistics Network formed when cables are connected*/
public interface ILogisticsNetwork extends ISonarListener, ISonarListenable<ILogisticsNetwork> {

	/**networking which provide connects to this one*/
    int CONNECTED_NETWORK = 0;
	
	/**networking which take info from this one*/
    int WATCHING_NETWORK = 1;
	
	/**the current network ID, this matches the ID of the {@link IDataCable}s which form this network*/
	int getNetworkID();
	
	/**called when the network is first created*/
	void onNetworkCreated();
	
	/**called on every server tick, used to update the network
	 * this shouldn't be called from anywhere but the main tick event*/
	void onNetworkTick();	
	
	/**called when the network is deleted, e.g. the cables which form it are destroyed
	 * this shouldn't be called from anywhere but the Cable Connection Manager*/
	void onNetworkRemoved();
	
	/**called by the Cable Connection Manager when the cables forming this network have been changed
	 * this shouldn't be called from anywhere but the Cable Connection Manager*/
	void onCablesChanged();

	/**called whenever a certain Caches should be updated,
	 * this can called at anytime by anything in the network which effects the listed caches
	 * avoid calling this excessively
	 * see {@link #onConnectionChanged(INetworkListener)}*/
	void onCacheChanged(CacheHandler... cache);
		
	/**checks the given INetworkListener is still valid and removes it if not, 
	 * this shouldn't typically be called outside of the {@link ILogisticsNetwork}*/
	boolean validateTile(INetworkListener listener);

	/** called when a certain connection has been changed, this notifies the relevant CacheHandlers for you.
	 * this can called at anytime by anything in the network which effects the listed caches
	 * avoid calling this excessively */
	void onConnectionChanged(INetworkListener tile);

	/**called by {@link IDataCable}s when a neighbouring connection is added
	 * see */
	void addConnection(INetworkListener tile);

	/**called by {@link IDataCable}s when a neighbouring connection is removed
	 * see */
	void removeConnection(INetworkListener tile);
	
	/**called by {@link IDataCable}s when a neighbouring info provider is added
	 * this adds a local {@link IInfoProvider} from another neighbouring network which can provide info to the tiles in this one */
	void addLocalInfoProvider(IInfoProvider monitor);

	/**called by {@link IDataCable}s when a neighbouring info provider is remove
	 * see {@link #addLocalInfoProvider(IInfoProvider)} for more info*/
	void removeLocalInfoProvider(IInfoProvider monitor);	
	
	/**sends a packet of all connections on the network to the given player, used for the 'Channels' tab on Logistics Connections
	 * typically called as the GUI is opened*/
	void sendConnectionsPacket(EntityPlayer player);

	/**returns a list of all connections connected to the networking, within the given CacheType space*/
	List<NodeConnection> getConnections(CacheType cacheType);	
	
	/**creates a connections list of block coords
	 * see {@link #sendConnectionsPacket(EntityPlayer)} which takes care of also sending the packet to a player*/
	InfoChangeableList<MonitoredBlockCoords> createConnectionsList(CacheType cacheType);
	
	/**gets the cached list of the given CacheHandler, within the given CacheType space*/
	<T> List<T> getCachedTiles(CacheHandler<T> handler, CacheType cacheType);
	
	/**gets the required {@link INetworkChannels}, this can be null*/
	@Nullable <T extends INetworkChannels> T getNetworkChannels(Class<T> channelClass);

	/**gets the required {@link INetworkChannels}, if it is null one will be created*/
	<T extends INetworkChannels> T getOrCreateNetworkChannels(Class<T> channelClass);
	
	/**gets a list of all connected local info providers*/
	List<IInfoProvider> getLocalInfoProviders();

	List<IInfoProvider> getGlobalInfoProviders();

	/**gets the first local info provider from the stored list provided in {@link #getLocalInfoProvider()}*/
	IInfoProvider getLocalInfoProvider();
	
	//// MONITORING \\\\
	/**returns the networking last tick time in nanoseconds*/
	default long getNetworkTickTime(){
		return 0;
	}
	

}