package sonar.logistics.api.filters;

import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.nodes.TransferType;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.network.SyncFilterList;

public interface IFilteredTile extends ILogicViewable, IConnectionNode, IChannelledTile, IInventoryFilter {

	public SyncFilterList getFilters();
	
	public NodeTransferMode getTransferMode();
	
	/**if this can transfer the given transfer type. this doesn't disable other nodes transferring to it though, Unless connection is disabled is on.*/
	public boolean isTransferEnabled(TransferType type);
	
	public void setTransferType(TransferType type, boolean enable);
	
	public BlockConnection getConnected();
	
	public boolean canConnectToNodeConnection();
	
	public void incrementTransferMode();
}
