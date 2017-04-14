package sonar.logistics.api.networks;

import sonar.core.api.utils.BlockCoords;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.core.listener.PlayerListener;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.utils.MonitoredList;

public interface INetworkListChannels<H extends INetworkListHandler> extends INetworkChannels<H>{
	
	public H getHandler();
	/* W.I.P
	
	public void onListUpdate(INetworkListChannels<H> source, NodeConnection node, MonitoredList list, boolean rapid);
	
	public void addSubChannels(INetworkListChannels<H> channels);
	
	public void removeSubChannels(INetworkListChannels<H> channels);
	
	public void addMasterChannels(INetworkListChannels<H> channels);
	
	public void removeMasterChannels(INetworkListChannels<H> channels);
	
	public void updateAllChannels(boolean updateMasters);
	*/

	public boolean isCoordsMonitored(BlockConnection connection);

	public boolean isEntityMonitored(EntityConnection connection);
}
