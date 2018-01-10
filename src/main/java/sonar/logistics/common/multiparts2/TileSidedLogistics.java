package sonar.logistics.common.multiparts2;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.capability.PL2Capabilities;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;

public abstract class TileSidedLogistics extends TileLogistics {

	@Override
	public EnumFacing getCableFace() {
		return EnumFacing.VALUES[getBlockMetadata()];
	}

	@Override
	public NetworkConnectionType canConnect(int networkID, EnumFacing dir, boolean internal) {
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? NetworkConnectionType.NETWORK : NetworkConnectionType.NONE;
	}
}
