package sonar.logistics.api.core.tiles.readers.channels;

import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;

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
