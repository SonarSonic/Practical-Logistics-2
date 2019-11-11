package sonar.logistics.core.tiles.readers.base;

import net.minecraft.util.EnumFacing;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.core.tiles.base.TileSidedLogistics;

public class TileAbstractNewReader extends TileSidedLogistics {

    //TODO SOURCE LISTS



    @Override
    public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
        return dir == this.getCableFace() ? EnumCableRenderSize.HALF // internal
                : EnumCableRenderSize.CABLE; // external
    }

    @Override
    public EnumCableConnection canConnect(int registryID, EnumCableConnectionType type, EnumFacing dir, boolean internal) {
        EnumFacing toCheck = internal ? dir : dir.getOpposite();
        return toCheck == getCableFace() ? EnumCableConnection.NETWORK : toCheck == getCableFace().getOpposite() ? EnumCableConnection.VISUAL : EnumCableConnection.NONE;
    }
}
