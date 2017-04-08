package sonar.logistics.api.cabling;

import net.minecraft.util.EnumFacing;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.render.ICableRenderer;

/** implemented on Tile Entities and Forge Multipart parts which are cables */
public interface IDataCable extends ICableRenderer, IWorldPosition, IConnectable {
	
	public void onConnectionAdded(ILogicTile tile, EnumFacing face);
	
	public void onConnectionRemoved(ILogicTile tile, EnumFacing face);
	
	/** the cable should check it's connections and see if it is connected to the correct ones */
	//public void refreshConnections();
	
	/**ensures all connections are connected to the same network*/
	//public void configureConnections(INetworkCache network);	
	
	/**if the IDataCable has any connections*/
	//public boolean hasConnections();
	
}
