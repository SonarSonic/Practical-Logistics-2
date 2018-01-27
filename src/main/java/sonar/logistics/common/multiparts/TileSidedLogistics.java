package sonar.logistics.common.multiparts;

import net.minecraft.util.EnumFacing;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.ConnectableType;

public abstract class TileSidedLogistics extends TileLogistics {

	@Override
	public EnumFacing getCableFace() {
		return EnumFacing.VALUES[getBlockMetadata()];
	}

	@Override
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		if(!type.isData()){
			return CableConnectionType.NONE;
		}
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? CableConnectionType.NETWORK : CableConnectionType.NONE;
	}
}
