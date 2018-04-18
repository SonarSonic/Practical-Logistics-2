package sonar.logistics.api.cabling;

import net.minecraft.item.ItemStack;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.states.TileMessage;


/** used to be LogicTile. implemented by Tile Entities which can connect to Data Cables */
public interface INetworkTile extends IWorldPosition, INetworkListener, ICableConnectable {

	/** gets the network cache's ID */
	int getNetworkID();
	
	/**the currently connected network*/
	ILogisticsNetwork getNetwork();
	
	/**this tiles identity on the network*/
	int getIdentity();		
	
	TileMessage[] getValidMessages();
	
	PL2Multiparts getMultipart();
	
	default void onTileAddition(){}
	
	default void onTileRemoval(){}
	
	default ItemStack getDisplayStack(){
		return ItemStack.EMPTY;
	}
}
