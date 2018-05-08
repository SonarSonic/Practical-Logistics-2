package sonar.logistics.core.tiles.displays.tiles.small;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.core.tiles.displays.tiles.BlockClickableDisplay;

public class BlockMiniDisplay extends BlockClickableDisplay {

	public static final double depth = 0.0625, height = depth * 8, width = depth * 8, length = depth * 1;
	public static final AxisAlignedBB DOWN_AXIS = new AxisAlignedBB(width/2, 0, width/2, 1-width/2, length, 1 - width/2);
	public static final AxisAlignedBB UP_AXIS = new AxisAlignedBB((width/2), 1, (width/2), 1 - (width/2), 1 - length, 1 - (width/2));
	public static final AxisAlignedBB NORTH_AXIS = new AxisAlignedBB((width) / 2, (height / 2), length, 1 - width / 2, 1 - (height / 2), 0);
	public static final AxisAlignedBB SOUTH_AXIS = new AxisAlignedBB((width) / 2, (height / 2), 1, 1 - width / 2, 1 - (height / 2), 1 - length);
	public static final AxisAlignedBB WEST_AXIS = new AxisAlignedBB(length, (height / 2), (width / 2), 0, 1 - (height / 2), 1 - (width / 2));
	public static final AxisAlignedBB EAST_AXIS = new AxisAlignedBB(1, (height / 2), (width / 2), 1 - length, 1 - (height / 2), 1 - (width / 2));
	public static final AxisAlignedBB[] AXIS = new AxisAlignedBB[] { DOWN_AXIS, UP_AXIS, NORTH_AXIS, SOUTH_AXIS, WEST_AXIS, EAST_AXIS };

	public BlockMiniDisplay() {
		super(PL2Multiparts.MINI_DISPLAY);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AXIS[getOrientation(state).ordinal()];
	}
}
