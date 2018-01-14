package sonar.logistics.common.multiparts.wireless;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.common.multiparts.BlockLogisticsSided;

public class BlockAbstractWireless extends BlockLogisticsSided {

	public BlockAbstractWireless(PL2Multiparts multipart) {
		super(multipart);
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (!world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileAbstractWireless && placer instanceof EntityPlayer) {//may cause crashes if player isn't an EntityPlayer
				((TileAbstractWireless) tile).setOwner((EntityPlayer) placer);
			}
		}
	}
}
