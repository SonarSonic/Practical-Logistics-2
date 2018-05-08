package sonar.logistics.base.tiles;

import net.minecraft.item.ItemStack;
import sonar.core.utils.IValidate;
import sonar.core.utils.IWorldPosition;
import sonar.core.utils.IWorldTile;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.core.tiles.connections.ICableConnectable;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.base.guidance.errors.ErrorMessage;


/** used to be LogicTile. implemented by Tile Entities which can connect to Data Cables */
public interface INetworkTile extends IWorldPosition, ICableConnectable, IValidate, IWorldTile {

	/** gets the handling cache's ID */
	int getNetworkID();
	
	/**the currently connected handling*/
	ILogisticsNetwork getNetwork();
	
	/**this base identity on the handling*/
	int getIdentity();		
	
	ErrorMessage[] getValidMessages();
	
	PL2Multiparts getMultipart();
	
	default void onTileAddition(){}
	
	default void onTileRemoval(){}

	void onNetworkConnect(ILogisticsNetwork network);

	void onNetworkDisconnect(ILogisticsNetwork network);
	
	default ItemStack getDisplayStack(){
		return ItemStack.EMPTY;
	}
}
