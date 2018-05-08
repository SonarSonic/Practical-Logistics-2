package sonar.logistics.base.filters;

import sonar.core.helpers.FluidHelper.ITankFilter;
import sonar.logistics.api.core.tiles.nodes.INode;
import sonar.logistics.api.core.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.core.tiles.nodes.TransferType;
import sonar.logistics.base.channels.BlockConnection;

public interface ITransferFilteredTile extends IFilteredTile, INode, ITankFilter {

	NodeTransferMode getTransferMode();
	
	/**if this can transfer the given transfer type. this doesn't disable other nodes transferring to it though, Unless connection is disabled is on.*/
    boolean isTransferEnabled(TransferType type);
	
	void setTransferType(TransferType type, boolean enable);
	
	BlockConnection getConnected();
	
	boolean canConnectToNodeConnection();
	
	void incrementTransferMode();
}
