package sonar.logistics.core.tiles.displays.tiles;

import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.base.utils.slots.EnumDisplayFaceSlot;
import sonar.logistics.core.tiles.base.BlockLogisticsSided;
import sonar.logistics.core.tiles.displays.DisplayHelper;
import sonar.logistics.core.tiles.displays.tiles.holographic.TileAbstractHolographicDisplay;

import javax.annotation.Nonnull;

public class BlockAbstractDisplay extends BlockLogisticsSided {

	public BlockAbstractDisplay(PL2Multiparts multipart) {
		super(multipart);
	}

	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, net.minecraft.client.particle.ParticleManager manager) {
		if (target.sideHit == state.getValue(SonarProperties.ORIENTATION)) {
			return true;
		}
		return super.addHitEffects(state, world, target, manager);
	}

	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		EnumFacing[] orientation = DisplayHelper.getScreenOrientation(placer, facing);
		return getStateFromMeta(orientation[0].ordinal());
	}

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		EnumFacing[] orientation = DisplayHelper.getScreenOrientation(placer, facing);
		return EnumDisplayFaceSlot.fromFace(orientation[0]);
	}

	@Override
	public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
		return EnumDisplayFaceSlot.fromFace(getOrientation(state));
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileAbstractDisplay) {
			TileAbstractDisplay display = (TileAbstractDisplay) tile;
			if(display.getGSI() == null){
				return false;
			}
			if (facing != state.getValue(SonarProperties.ORIENTATION)) {
				if (!world.isRemote && canOpenGui(player)) {
					display.openFlexibleGui(player, 0);
				}
			} else {
				if(display instanceof TileAbstractHolographicDisplay){
					///holographic scaling gui
					display.openFlexibleGui(player, 2);
					return true;
				}

				return display.getGSI().onClicked(display, player.isSneaking() ? BlockInteractionType.SHIFT_RIGHT : BlockInteractionType.RIGHT, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
			}
		}
		return true;

	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
		EnumFacing rotation = EnumFacing.NORTH;
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileAbstractDisplay) {
			//FIXME
			//rotation = ((TileAbstractDisplay) tile).getGSI().getRotation();
		}
		return state.withProperty(SonarProperties.ROTATION, rotation);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SonarProperties.ORIENTATION, SonarProperties.ROTATION);
	}

}