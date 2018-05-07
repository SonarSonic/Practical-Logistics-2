package sonar.logistics.api.tiles;

import net.minecraft.item.ItemStack;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.cabling.ICableConnectable;
import sonar.logistics.api.errors.ErrorMessage;
import sonar.logistics.api.networking.ILogisticsNetwork;
import sonar.logistics.api.networking.INetworkListener;


/** used to be LogicTile. implemented by Tile Entities which can connect to Data Cables */
public interface INetworkTile extends IWorldPosition, INetworkListener, ICableConnectable {

	/** gets the network cache's ID */
	int getNetworkID();
	
	/**the currently connected network*/
	ILogisticsNetwork getNetwork();
	
	/**this tiles identity on the network*/
	int getIdentity();		
	
	ErrorMessage[] getValidMessages();
	
	PL2Multiparts getMultipart();
	
	default void onTileAddition(){}
	
	default void onTileRemoval(){}
	
	default ItemStack getDisplayStack(){
		return ItemStack.EMPTY;
	}
}
