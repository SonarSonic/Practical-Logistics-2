package sonar.logistics.api.core.tiles.connections.data.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.logistics.api.core.tiles.connections.data.IDataCable;
import sonar.logistics.api.core.tiles.readers.IInfoProvider;
import sonar.logistics.api.core.tiles.readers.channels.INetworkChannels;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.core.tiles.connections.data.network.CacheHandler;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;

import javax.annotation.Nullable;
import java.util.List;

/**the default implementation of a Logistics Network formed when connections are connected*/
public interface ILogisticsNetwork extends ISonarListener, ISonarListenable<ILogisticsNetwork> {

	/**connections which provide connects to this one*/
    int CONNECTED_NETWORK = 0;
	
	/**connections which take info from this one*/
    int WATCHING_NETWORK = 1;
	
	/**the current handling ID, this matches the ID of the {@link IDataCable}s which form this handling*/
	int getNetworkID();
	
	/**called when the handling is first created*/
	void onNetworkCreated();
	
	/**called on every server tick, used to update the handling
	 * this shouldn't be called from anywhere but the main tick event*/
	void onNetworkTick();	
	
	/**called when the handling is deleted, e.g. the connections which form it are destroyed
	 * this shouldn't be called from anywhere but the Cable Connection Manager*/
	void onNetworkRemoved();

	/**checks the given INetworkListener is still valid and removes it if not, 
	 * this shouldn't typically be called outside of the {@link ILogisticsNetwork}*/
	boolean validateTile(INetworkTile listener);

	/**called by {@link IDataCable}s when a neighbouring connection is added
	 * see */
	void addConnection(INetworkTile tile);

	/**called by {@link IDataCable}s when a neighbouring connection is removed
	 * see */
	void removeConnection(INetworkTile tile);
	
	/**called by {@link IDataCable}s when a neighbouring info provider is added
	 * this adds a local {@link IInfoProvider} from another neighbouring handling which can provide info to the base in this one */
	void addLocalInfoProvider(IInfoProvider monitor);

	/**called by {@link IDataCable}s when a neighbouring info provider is remove
	 * see {@link #addLocalInfoProvider(IInfoProvider)} for more info*/
	void removeLocalInfoProvider(IInfoProvider monitor);	
	
	/**sends a packet of all connections on the handling to the given player, used for the 'Channels' tab on Logistics Connections
	 * typically called as the GUI is opened*/
	void sendConnectionsPacket(EntityPlayer player);

	/**returns a list of all connections connected to the connections, within the given CacheType space*/
	List<NodeConnection> getConnections(CacheType cacheType);	
	
	/**creates a connections list of block coords
	 * see {@link #sendConnectionsPacket(EntityPlayer)} which takes care of also sending the packet to a player*/
	InfoChangeableList<MonitoredBlockCoords> createConnectionsList(CacheType cacheType);
	
	/**gets the cached list of the given CacheHandler, within the given CacheType space*/
	<T extends INetworkTile> List<T> getCachedTiles(CacheHandler<T> handler, CacheType cacheType);
	
	/**gets the required {@link INetworkChannels}, this can be null*/
	@Nullable <T extends INetworkChannels> T getNetworkChannels(Class<T> channelClass);

	/**gets the required {@link INetworkChannels}, if it is null one will be created*/
	<T extends INetworkChannels> T getOrCreateNetworkChannels(Class<T> channelClass);
	
	/**gets a list of all connected local info providers*/
	List<IInfoProvider> getLocalInfoProviders();

	List<IInfoProvider> getGlobalInfoProviders();

	IItemHandler getNetworkItemHandler();
	
	//// MONITORING \\\\
	/**returns the connections last tick time in nanoseconds*/
	default long getNetworkTickTime(){
		return 0;
	}
	

}