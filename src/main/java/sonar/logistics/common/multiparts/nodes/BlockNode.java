package sonar.logistics.common.multiparts.nodes;

import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.common.multiparts.BlockLogisticsSided;

public class BlockNode extends BlockLogisticsSided {

	public BlockNode() {
		super(PL2Multiparts.NODE);
	}
	
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getStateFromMeta(facing.getOpposite().ordinal());
	}

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		return EnumFaceSlot.fromFace(facing.getOpposite());
	}

}
