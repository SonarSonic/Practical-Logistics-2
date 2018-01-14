package sonar.logistics.common.multiparts.misc;

import javax.annotation.Nullable;

import net.minecraft.block.properties.IProperty;
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

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return side != state.getValue(SonarProperties.ORIENTATION).getOpposite();
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		if (side != state.getValue(SonarProperties.ORIENTATION).getOpposite()) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && tile instanceof TileClock) {
				return ((TileClock) tile).powering ? 15 : 0;
			}
		}
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return getWeakPower(state, world, pos, side);
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(PL2Properties.CLOCK_HAND, false);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SonarProperties.ORIENTATION, PL2Properties.CLOCK_HAND });
	}

}
