package sonar.logistics.common.multiparts2.misc;

import net.minecraft.util.EnumFacing;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.common.multiparts2.TileSidedLogistics;

public class TileRedstoneSignaller extends TileSidedLogistics {

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return null;
	}

}
