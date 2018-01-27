package sonar.logistics.common.multiparts.wireless;

import sonar.logistics.PL2Multiparts;

public class BlockRedstoneReceiver extends BlockAbstractWireless {

	public BlockRedstoneReceiver() {
		super(PL2Multiparts.REDSTONE_RECEIVER);
	}

	//// REDSTONE \\\\
	/*
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
	*/

}
