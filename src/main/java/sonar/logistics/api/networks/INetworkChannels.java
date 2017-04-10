package sonar.logistics.api.networks;

import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.connections.CacheHandler;

public interface INetworkChannels<H extends INetworkHandler> {

	public void onCreated();
	
	public ILogisticsNetwork getNetwork();
	
	public H getHandler();
	
	public CacheHandler[] getValidCaches();
	
	public void addConnection(CacheHandler cache, INetworkListener connection);

	public void removeConnection(CacheHandler cache, INetworkListener connection);
	
	public void createChannelLists();
	
	public void updateChannelLists();
	
	public void onDeleted();
}
