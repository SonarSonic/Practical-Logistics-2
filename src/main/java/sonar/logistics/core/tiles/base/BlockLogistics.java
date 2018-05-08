package sonar.logistics.core.tiles.base;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.common.block.SonarMaterials;
import sonar.core.integration.multipart.BlockSonarMultipart;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.base.utils.LogisticsHelper;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;

import javax.annotation.Nonnull;

public abstract class BlockLogistics extends BlockSonarMultipart {

	public PL2Multiparts multipart;

	public BlockLogistics(PL2Multiparts multipart) {
		super(SonarMaterials.machine);
		this.multipart = multipart;
	}

	public PL2Multiparts getMultipart() {
		return multipart;
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World var1, int var2) {
		return getMultipart().createTileEntity();
	}

	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return getBoundingBox(state, world, pos);
	}

	public boolean hasStandardGui() {
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hasStandardGui() && canOpenGui(player)) {
			TileEntity tile = world.getTileEntity(pos);
			if (!tile.getWorld().isRemote && tile instanceof TileSonarMultipart) {
				((TileSonarMultipart) tile).openFlexibleGui(player, 0);
			}
			return true;
		}
		return false;
	}

	public boolean canOpenGui(EntityPlayer player) {
        return !LogisticsHelper.isPlayerUsingOperator(player);
    }

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileLogistics && !world.isRemote) {
			((TileLogistics)tile).doAdditionEvent(PL2AdditionType.PLAYER_ADDED);
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}

	@Override
	public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileLogistics && !world.isRemote) {
			((TileLogistics)tile).doRemovalEvent(PL2RemovalType.PLAYER_REMOVED);		
		}
		super.breakBlock(world, pos, state);
	}
}
