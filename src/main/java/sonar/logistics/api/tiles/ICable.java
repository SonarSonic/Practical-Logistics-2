package sonar.logistics.api.tiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;

/** implemented on multiparts which can join together, used by Large Display Screens and cables */
public interface ICable extends IWorldPosition {

	/** is the cable limited by the number of channels, true for Channelled Cables, false for Data Cables */
	public ConnectableType getConnectableType();

	/** called when the cable is added to the world */
	//public void addConnection();

	/** called when the cable is removed to the world */
	//public void removeConnection();

	/** returns the ID of this connection's network */
	public int getRegistryID();

	/** sets the ID of this connection. Shouldn't be called outside of the ConnectionManager */
	public void setRegistryID(int id);

	/** can the Tile connect to cables on the given direction
	 * @param networkID the id of the connection trying to connect
	 * @param dir the direction it is connecting from
	 * @param internal if this cable is within the bounds of this block.
	 * @return if the tile can connect. */
	//public boolean canConnectOnSide(int networkID, EnumFacing dir, boolean internal); //REPLACED WITH INETWORKCONNECTION METHOD
	public NetworkConnectionType canConnect(int networkID, EnumFacing dir, boolean internal);
	
	public boolean isBlocked(EnumFacing dir);
}
