package sonar.logistics.common.multiparts.wireless;

import sonar.logistics.PL2Multiparts;

public class BlockRedstoneEmitter extends BlockAbstractWireless {

	public BlockRedstoneEmitter() {
		super(PL2Multiparts.REDSTONE_EMITTER);
	}
	/*
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		TileEntity t = world.getTileEntity(pos);
		if (t != null && t instanceof TileRedstoneEmitter) {
			((TileRedstoneEmitter) t).onNeighbouringBlockChanged();
		}
	}
	*/
}
