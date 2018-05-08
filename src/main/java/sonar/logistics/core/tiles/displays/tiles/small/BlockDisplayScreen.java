package sonar.logistics.core.tiles.displays.tiles.small;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.core.tiles.displays.tiles.BlockClickableDisplay;

public class BlockDisplayScreen extends BlockClickableDisplay {

	public static final double depth = 0.0625, height = depth * 16, width = 0, length = depth * 1;
	public static final AxisAlignedBB DOWN_AXIS = new AxisAlignedBB(0, 0, 0, 1, depth, 1);
	public static final AxisAlignedBB UP_AXIS = new AxisAlignedBB(0, 1, 0, 1, 1 - depth, 1);
	public static final AxisAlignedBB NORTH_AXIS = new AxisAlignedBB((width) / 2, depth * 4, length, 1 - width / 2, 1 - depth * 4, 0);
	public static final AxisAlignedBB SOUTH_AXIS = new AxisAlignedBB((width) / 2, depth * 4, 1, 1 - width / 2, 1 - depth * 4, 1 - length);
	public static final AxisAlignedBB WEST_AXIS = new AxisAlignedBB(length, depth * 4, (width) / 2, 0, 1 - depth * 4, 1 - width / 2);
	public static final AxisAlignedBB EAST_AXIS = new AxisAlignedBB(1, depth * 4, (width) / 2, 1 - length, 1 - depth * 4, 1 - width / 2);
	public static final AxisAlignedBB[] AXIS = new AxisAlignedBB[] { DOWN_AXIS, UP_AXIS, NORTH_AXIS, SOUTH_AXIS, WEST_AXIS, EAST_AXIS };

	public BlockDisplayScreen() {
		super(PL2Multiparts.DISPLAY_SCREEN);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AXIS[getOrientation(state).ordinal()];
	}
}
