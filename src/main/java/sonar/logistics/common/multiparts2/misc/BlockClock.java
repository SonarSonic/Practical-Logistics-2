package sonar.logistics.common.multiparts2.misc;

import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.common.multiparts2.BlockLogisticsMultipart;

public class BlockClock extends BlockLogisticsMultipart {

	public BlockClock(PL2Multiparts multipart) {
		super(multipart);
	}

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
		// TODO Auto-generated method stub
		return null;
	}

}
