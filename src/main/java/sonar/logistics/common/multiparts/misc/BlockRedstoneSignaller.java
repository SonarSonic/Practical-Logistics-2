package sonar.logistics.common.multiparts.misc;

import javax.annotation.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.common.multiparts.BlockLogisticsSided;

public class BlockRedstoneSignaller extends BlockLogisticsSided {

	public BlockRedstoneSignaller() {
		super(PL2Multiparts.REDSTONE_SIGNALLER);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return true;
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileRedstoneSignaller) {
			return ((TileRedstoneSignaller) tile).isActive() ? 15 : 0;
		}
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return getWeakPower(state, world, pos, side);
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		boolean active = false;
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileRedstoneSignaller) {
			active = ((TileRedstoneSignaller) tile).isActive();
		}
		return state.withProperty(PL2Properties.ACTIVE, active);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SonarProperties.ORIENTATION, PL2Properties.ACTIVE });
	}

}