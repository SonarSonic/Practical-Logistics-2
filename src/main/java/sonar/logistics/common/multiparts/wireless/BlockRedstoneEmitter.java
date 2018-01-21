package sonar.logistics.common.multiparts.wireless;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;

public class BlockRedstoneEmitter extends BlockAbstractWireless {

	public BlockRedstoneEmitter() {
		super(PL2Multiparts.REDSTONE_EMITTER);
	}

	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		TileEntity t = world.getTileEntity(pos);
		if (t != null && t instanceof TileRedstoneEmitter) {
			((TileRedstoneEmitter) t).onNeighbouringBlockChanged();
		}
	}
}
