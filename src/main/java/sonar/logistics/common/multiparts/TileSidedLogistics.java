package sonar.logistics.common.multiparts;

import net.minecraft.util.EnumFacing;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;

public abstract class TileSidedLogistics extends TileLogistics {

	@Override
	public EnumFacing getCableFace() {
		return EnumFacing.VALUES[getBlockMetadata()];
	}

	@Override
	public NetworkConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? NetworkConnectionType.NETWORK : NetworkConnectionType.NONE;
	}
}
