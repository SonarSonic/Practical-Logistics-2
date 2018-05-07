package sonar.logistics.api.networking;

import sonar.logistics.networking.CacheHandler;

/**network channels are used by {@link ILogisticsNetwork} to perform certain operators for a network,
 * for example channels will update reader info and run the Transfer Node network.
 * channels are only created if the type of connections it works with are also connected, this optimises the network*/
public interface INetworkChannels {
	
	/**the network which created this INetworkChannels and which it links to.*/
    ILogisticsNetwork getNetwork();
	
	/**the cache types that this INetworkChannels can use*/
    CacheHandler[] getValidCaches();
	
	/**called when the channel is first created*/
    void onCreated();

	/**called when the channel is deleted, use this for clearing any lists*/
    void onDeleted();
	
	/**called when the connected {@link ILogisticsNetwork}s channels (blocks/entities it connects to) are changed*/
    void onChannelsChanged();
	
	/**called when a connection is added to a valid CacheHandler, 
	 * this will only has CacheHandlers this Channel can connect to see {@link INetworkChannels#getValidCaches()}*/
    void addConnection(CacheHandler cache, INetworkListener connection);

	/**called when a connection is removed from a valid CacheHandler*/
    void removeConnection(CacheHandler cache, INetworkListener connection);
	
	/**called when the {@link ILogisticsNetwork} is updated, 
	 * after all other operations on the network have been performed
	 * this method should be used to update the channel*/
    void updateChannel();
}
