package sonar.logistics.api.cabling;

import java.util.UUID;

import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.connecting.INetworkListener;

/** implemented by Tile Entities which can connect to Data Cables */
public interface ILogicTile extends IWorldPosition, INetworkListener {

	/** can the Tile connect to cables on the given direction */
	public NetworkConnectionType canConnect(EnumFacing dir);

	/** gets the network cache's ID */
	public int getNetworkID();
	
	/**the currently connected network*/
	public ILogisticsNetwork getNetwork();
	
	///get part UUID
	public UUID getUUID();		
	
	///get part identity
	public int getIdentity();		
}
