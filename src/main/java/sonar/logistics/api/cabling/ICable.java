package sonar.logistics.api.cabling;

import net.minecraft.util.EnumFacing;
import sonar.core.utils.IWorldPosition;

/** implemented on multiparts which can join together, used by Large Display Screens and cables */
public interface ICable extends IWorldPosition, ICableConnectable {

	/** is the cable limited by the number of channels, true for Channelled Cables, false for Data Cables */
    ConnectableType getConnectableType();

	/** returns the ID of this connection's network */
    int getRegistryID();

	/** sets the ID of this connection. Shouldn't be called outside of the ConnectionManager */
    void setRegistryID(int id);

	/** can the Tile connect to cables on the given direction
	 * @param registryID the id of the connection trying to connect
	 * @param type TODO
	 * @param dir the direction it is connecting from
	 * @param internal if this cable is within the bounds of this block.
	 * @return if the tile can connect. */
    CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal);
	
	boolean isBlocked(EnumFacing dir);
}
