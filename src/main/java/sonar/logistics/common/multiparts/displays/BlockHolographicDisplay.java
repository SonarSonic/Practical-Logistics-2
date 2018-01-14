package sonar.logistics.common.multiparts.displays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;

public class BlockHolographicDisplay extends BlockAbstractDisplay {

	public BlockHolographicDisplay() {
		super(PL2Multiparts.HOLOGRAPHIC_DISPLAY);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		double p = 0.0625;
		double height = p * 16, width = 0, length = p * 1;
		EnumFacing face = state.getValue(SonarProperties.ORIENTATION);
		switch (face) {
		case EAST:
			return new AxisAlignedBB(1, p * 4, (width) / 2, 1 - length, 1 - p * 4, 1 - width / 2);
		case NORTH:
			return new AxisAlignedBB((width) / 2, p * 4, length, 1 - width / 2, 1 - p * 4, 0);
		case SOUTH:
			return new AxisAlignedBB((width) / 2, p * 4, 1, 1 - width / 2, 1 - p * 4, 1 - length);
		case WEST:
			return new AxisAlignedBB(length, p * 4, (width) / 2, 0, 1 - p * 4, 1 - width / 2);
		case UP:
			return new AxisAlignedBB(0, 1 - 0, 0, 1, 1 - 0.0625, 1);
		default:
			return new AxisAlignedBB(0, 0, 0, 1, 0.0625, 1);
		}
	}
}
