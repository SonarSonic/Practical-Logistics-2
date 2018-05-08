package sonar.logistics.api.core.tiles.connections;

import net.minecraft.util.EnumFacing;
import sonar.core.utils.IWorldTile;

/**used on Tiles which can connect to cables, this includes cables themselves.*/
public interface ICableConnectable extends IWorldTile {

    /** can the Tile connect to connections on the given direction
     * @param registryID the id of the connection trying to connect
     * @param type TODO
     * @param dir the direction it is connecting from
     * @param internal if this cable is within the bounds of this block.
     * @return if the tile can connect. */
    EnumCableConnection canConnect(int registryID, EnumCableConnectionType type, EnumFacing dir, boolean internal);

	/** for internal connections */
    EnumCableRenderSize getCableRenderSize(EnumFacing dir);
}
