package sonar.logistics.api.core.tiles.connections;

import net.minecraft.util.EnumFacing;
import sonar.core.utils.IWorldPosition;

/** implemented on multiparts which can join together, used by Large Display Screens and connections */
public interface ICable extends IWorldPosition, ICableConnectable {

	/** is the cable limited by the number of channels, true for Channelled Cables, false for Data Cables */
    EnumCableConnectionType getConnectableType();

	/** returns the ID of this connection's handling */
    int getRegistryID();

	/** sets the ID of this connection. Shouldn't be called outside of the ConnectionManager */
    void setRegistryID(int id);

	
	boolean isBlocked(EnumFacing dir);
}
