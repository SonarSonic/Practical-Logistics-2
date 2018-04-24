package sonar.logistics.common.multiparts.misc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.common.multiparts.BlockLogisticsSided;

public class BlockClock extends BlockLogisticsSided {

	public BlockClock() {
		super(PL2Multiparts.CLOCK);
	}

	public boolean isPowering(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileClock) {
			return ((TileClock) tile).powering;
		}
		return false;
	}

	//// REDSTONE \\\\
	
	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		if (side != getOrientation(state).getOpposite()) {
			return isPowering(world, pos) ? 15 : 0;
		}
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return getWeakPower(state, world, pos, side);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return side != getOrientation(state).getOpposite();
	}

	//// STATE \\\\

	@Nonnull
    @Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(PL2Properties.CLOCK_HAND, false);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SonarProperties.ORIENTATION, PL2Properties.CLOCK_HAND);
	}

}
