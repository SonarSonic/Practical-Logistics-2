package sonar.logistics.api.networks;

import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;

public interface INetworkListChannels<H extends INetworkListHandler> extends INetworkChannels{
	
	H getHandler();
	/* W.I.P
	
	public void onListUpdate(INetworkListChannels<H> source, NodeConnection node, MonitoredList list, boolean rapid);
	
	public void addSubChannels(INetworkListChannels<H> channels);
	
	public void removeSubChannels(INetworkListChannels<H> channels);
	
	public void addMasterChannels(INetworkListChannels<H> channels);
	
	public void removeMasterChannels(INetworkListChannels<H> channels);
	
	public void updateAllChannels(boolean updateMasters);
	*/

	boolean isCoordsMonitored(BlockConnection connection);

	boolean isEntityMonitored(EntityConnection connection);
}
