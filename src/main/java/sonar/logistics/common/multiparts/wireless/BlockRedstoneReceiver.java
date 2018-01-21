package sonar.logistics.common.multiparts.wireless;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.common.multiparts.misc.TileRedstoneSignaller;

public class BlockRedstoneReceiver extends BlockAbstractWireless {

	public BlockRedstoneReceiver() {
		super(PL2Multiparts.REDSTONE_RECEIVER);
	}

	//// REDSTONE \\\\
	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileRedstoneReceiver) {
			return ((TileRedstoneReceiver) tile).getRedstonePower();
		}
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return getWeakPower(state, world, pos, side);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return side == state.getValue(SonarProperties.ORIENTATION);
	}

}
