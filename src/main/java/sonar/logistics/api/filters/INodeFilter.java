package sonar.logistics.api.filters;

import sonar.core.api.nbt.INBTSyncable;
import sonar.core.client.gui.GuiSonar;
import sonar.core.network.sync.ISyncableListener;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.tiles.nodes.TransferType;

public interface INodeFilter extends ISyncableListener, INBTSyncable {
	
	String getNodeID();

	TransferType[] getTypes();
	
	NodeTransferMode getTransferMode();
		
	void renderInfoInList(GuiSonar screen, int yPos);
	
	FilterList getListType();
	
	boolean isValidFilter();
	
}
