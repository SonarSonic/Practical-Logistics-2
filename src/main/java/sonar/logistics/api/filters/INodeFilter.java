package sonar.logistics.api.filters;

import sonar.core.api.nbt.INBTSyncable;
import sonar.core.client.gui.GuiSonar;
import sonar.core.network.sync.ISyncableListener;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.tiles.nodes.TransferType;

public interface INodeFilter extends ISyncableListener, INBTSyncable {
	
	public String getNodeID();

	public TransferType[] getTypes();
	
	public NodeTransferMode getTransferMode();
		
	public void renderInfoInList(GuiSonar screen, int yPos);
	
	public FilterList getListType();
	
	public boolean isValidFilter();
	
}
