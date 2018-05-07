package sonar.logistics.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import sonar.core.SonarCore;
import sonar.logistics.PL2Blocks;
import sonar.logistics.common.hammer.TileEntityHammer;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockHammerAir extends Block {

	public BlockHammerAir() {
		super(Material.CLOTH);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		for (int i = 1; i < 3; i++) {
			BlockPos adj = pos.offset(EnumFacing.DOWN, i);
			TileEntity tile = world.getTileEntity(adj);
			if (tile instanceof TileEntityHammer) {
				SonarCore.instance.guiHandler.openBasicTile(false, tile, player, world, adj, 0);
				return true;
			}
		}
		return false;

	}

	//// EVENTS \\\\

	@Override
	public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		world.setBlockToAir(pos);
		for (int i = 1; i < 3; i++) {
			BlockPos adj = pos.offset(EnumFacing.DOWN, i);
			IBlockState hammerState = world.getBlockState(adj);
			Block block = hammerState.getBlock();
			if (block == PL2Blocks.hammer) {
				block.dropBlockAsItem(world, adj, hammerState, 0);
				world.setBlockToAir(adj);
			}
		}
	}

	//// RENDERING \\\\

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Nonnull
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	//// DROPS \\\\

	@Override
	public int quantityDropped(Random p_149745_1_) {
		return 0;
	}

	@Nonnull
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		return new ItemStack(PL2Blocks.hammer, 1, 0);
	}
}
