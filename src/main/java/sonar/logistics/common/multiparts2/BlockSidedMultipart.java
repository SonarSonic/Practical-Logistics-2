package sonar.logistics.common.multiparts2;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;

//drops

public class BlockSidedMultipart extends BlockLogisticsMultipart {


	public BlockSidedMultipart(PL2Multiparts multipart) {
		super(multipart);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return PL2Properties.getStandardBox(getFaceFromState(state), getMultipart());
	}
	
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getStateFromMeta(facing.getOpposite().ordinal());
	}

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		return EnumFaceSlot.fromFace(facing.getOpposite());
	}

	@Override
	public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
		return EnumFaceSlot.fromFace(getFaceFromState(state));
	}
	
	public EnumFacing getFaceFromState(IBlockState state) {
		return state.getValue(SonarProperties.ORIENTATION);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.VALUES[meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return getFaceFromState(state).ordinal();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SonarProperties.ORIENTATION);
	}
	
}
