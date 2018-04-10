package sonar.logistics.common.multiparts.displays;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.properties.IProperty;
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
import sonar.core.helpers.RayTraceHelper;
import sonar.logistics.PL2Events;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.common.multiparts.BlockLogisticsSided;
import sonar.logistics.networking.displays.DisplayHelper;

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
		if (tile != null && tile instanceof TileAbstractDisplay) {
			TileAbstractDisplay display = (TileAbstractDisplay) tile;
			if (facing != state.getValue(SonarProperties.ORIENTATION)) {
				if (!world.isRemote && canOpenGui(player)) {
					display.openFlexibleGui(player, 0);
				}
			} else {
				return display.getGSI().onClicked(display, player.isSneaking() ? BlockInteractionType.SHIFT_RIGHT : BlockInteractionType.RIGHT, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
			}
		}
		return true;

	}

	public boolean canPlayerDestroy(IPartInfo part, EntityPlayer player) {
		boolean canDestroy = canPlayerDestroy(part.getState(), part.getActualWorld(), part.getPartPos(), player);
		if (!canDestroy) {
			onBlockClicked(part.getPartWorld(), part.getPartPos(), player);
		}
		return canDestroy;
	}

	public boolean canPlayerDestroy(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
		RayTraceResult rayResult = RayTraceHelper.getRayTraceEyes(player, world);
		if (rayResult == null || state.getValue(SonarProperties.ORIENTATION).getOpposite() == rayResult.sideHit) {
			return true;
		}
		return false;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (!canPlayerDestroy(state, world, pos, player)) {
			onBlockClicked(world, pos, player);
			return false;
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (PL2Events.coolDownClick == 0) {
			PL2Events.coolDownClick = 2;
			RayTraceResult rayResult = RayTraceHelper.getRayTraceEyes(player, world);
			TileAbstractDisplay display = (TileAbstractDisplay) world.getTileEntity(pos);
			float hitX = (float) (rayResult.hitVec.x - (double) pos.getX());
			float hitY = (float) (rayResult.hitVec.y - (double) pos.getY());
			float hitZ = (float) (rayResult.hitVec.z - (double) pos.getZ());
			if (display.getGSI() != null)
				display.getGSI().onClicked(display, player.isSneaking() ? BlockInteractionType.SHIFT_LEFT : BlockInteractionType.LEFT, world, pos, world.getBlockState(pos), player, player.getActiveHand(), display.getCableFace(), hitX, hitY, hitZ);

		}

	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		EnumFacing rotation = EnumFacing.NORTH;
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileAbstractDisplay) {
			rotation = ((TileAbstractDisplay) tile).getGSI().getRotation();
		}
		return state.withProperty(SonarProperties.ROTATION, rotation);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SonarProperties.ORIENTATION, SonarProperties.ROTATION });
	}

}