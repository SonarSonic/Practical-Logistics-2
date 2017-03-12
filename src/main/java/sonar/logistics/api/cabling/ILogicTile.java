package sonar.logistics.api.cabling;

import java.util.UUID;

import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.connecting.INetworkCache;

/** implemented by Tile Entities which can connect to Data Cables */
public interface ILogicTile extends IWorldPosition {

	/** can the Tile connect to cables on the given direction */
	public NetworkConnectionType canConnect(EnumFacing dir);

	/** the {@link BlockCoords} this Block/FMP Part should be registered as on the Network
	 * 
	 * @return the {@link BlockCoords} */
	public BlockCoords getCoords();

	/** gets the network cache's ID */
	public int getNetworkID();
	
	/**the currently connected network*/
	public INetworkCache getNetwork();

	/** sets the network this tile is connected to */
	public void setLocalNetworkCache(INetworkCache network);
	
	///get part UUID
	public UUID getUUID();		
}
