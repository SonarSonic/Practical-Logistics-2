package sonar.logistics.common.multiparts.nodes;

import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.PL2Properties;
import sonar.logistics.common.multiparts.BlockLogisticsSided;
import sonar.logistics.networking.cabling.RedstoneCableHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockRedstoneNode extends BlockLogisticsSided {

	public BlockRedstoneNode() {
		super(PL2Multiparts.REDSTONE_NODE);
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (!tile.getWorld().isRemote && tile instanceof TileRedstoneNode) {
			TileRedstoneNode node = (TileRedstoneNode) tile;
			if(node.isSelfChecking){ //avoids the node reading it's own power
				return 0;
			}
		}
		if (side == state.getValue(SonarProperties.ORIENTATION).getOpposite()) {
			return RedstoneCableHelper.getCableState(world, pos).getValue(PL2Properties.ACTIVE) ? 15 : 0;
		}
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return getWeakPower(state, world, pos, side);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return state.getValue(SonarProperties.ORIENTATION).getOpposite() == side;
	}

	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getStateFromMeta(facing.getOpposite().ordinal());
	}

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		return EnumFaceSlot.fromFace(facing.getOpposite());
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
		int power = getWeakPower(state, world, pos, state.getValue(SonarProperties.ORIENTATION).getOpposite());
		if (power == 0) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileRedstoneNode) {
				power = ((TileRedstoneNode) tile).getCurrentPower();
			}
		}
		return state.withProperty(PL2Properties.ACTIVE, power > 0);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SonarProperties.ORIENTATION, PL2Properties.ACTIVE);
	}
}
