package sonar.logistics.core.tiles.base;

import net.minecraft.util.EnumFacing;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;

public abstract class TileSidedLogistics extends TileLogistics {

	@Override
	public EnumFacing getCableFace() {
		return EnumFacing.VALUES[getBlockMetadata()];
	}

	@Override
	public EnumCableConnection canConnect(int registryID, EnumCableConnectionType type, EnumFacing dir, boolean internal) {
		if(!type.isData()){
			return EnumCableConnection.NONE;
		}
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? EnumCableConnection.NETWORK : EnumCableConnection.NONE;
	}
}
