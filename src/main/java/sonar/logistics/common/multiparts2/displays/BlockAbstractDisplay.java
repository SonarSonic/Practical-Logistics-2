package sonar.logistics.common.multiparts2.displays;

import javax.annotation.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.common.multiparts2.BlockLogisticsSided;

public class BlockAbstractDisplay extends BlockLogisticsSided {

	public BlockAbstractDisplay(PL2Multiparts multipart) {
		super(multipart);
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (canOpenGui(player) && !world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && tile instanceof TileAbstractDisplay) {
				TileAbstractDisplay display = (TileAbstractDisplay) tile;
				if (facing != state.getValue(SonarProperties.ORIENTATION)) {
					display.openFlexibleGui(player, 0);
				} else {
					return display.container().onClicked(display, player.isSneaking() ? BlockInteractionType.SHIFT_RIGHT : BlockInteractionType.RIGHT, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
				}
			}
		}
		return true;
	}

	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		super.harvestBlock(world, player, pos, state, te, stack);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		EnumFacing rotation = EnumFacing.NORTH;
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileAbstractDisplay) {
			rotation = ((TileAbstractDisplay) tile).rotation;
		}

		return state.withProperty(SonarProperties.ROTATION, rotation);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SonarProperties.ORIENTATION, SonarProperties.ROTATION });
	}

}
