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
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.common.multiparts.BlockLogisticsSided;

import javax.annotation.Nonnull;

public class BlockTransferNode extends BlockLogisticsSided {

	public BlockTransferNode() {
		super(PL2Multiparts.TRANSFER_NODE);
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
		NodeTransferMode mode = NodeTransferMode.ADD;
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileTransferNode){
			mode = ((TileTransferNode)tile).getTransferMode();
		}
		return state.withProperty(PL2Properties.TRANSFER, mode);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SonarProperties.ORIENTATION, PL2Properties.TRANSFER);
	}
}
