package sonar.logistics.api.cabling;

import net.minecraft.util.EnumFacing;
import sonar.core.utils.IWorldTile;

public interface ICableConnectable extends IWorldTile {

	/** can the Tile connect to cables on the given direction */
    CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal);

	/** for internal connections */
    CableRenderType getCableRenderSize(EnumFacing dir);
}
