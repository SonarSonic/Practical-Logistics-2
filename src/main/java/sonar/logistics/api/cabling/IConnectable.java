package sonar.logistics.api.cabling;

import net.minecraft.util.EnumFacing;
import sonar.core.utils.IWorldPosition;

/**implemented on multiparts which can join together, used by Large Display Screens and cables*/
public interface IConnectable extends IWorldPosition {

	/** is the cable limited by the number of channels, true for Channelled Cables, false for Data Cables */
	public ConnectableType getCableType();
	
	/** called when the cable is added to the world */
	public void addToNetwork();

	/** called when the cable is removed to the world */
	public void removeFromNetwork();	
	
	/**returns the ID of this connection's network*/
	public int getRegistryID();

	/**sets the ID of this connection. Shouldn't be called outside of the ConnectionManager*/
	public void setRegistryID(int id);
	
	/** can the Tile connect to cables on the given direction 
	 * @param connectingID TODO*/
	public boolean canConnectOnSide(int connectingID, EnumFacing dir);
}
