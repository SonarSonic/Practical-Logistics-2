package sonar.logistics.common.multiparts.readers;

import mcmultipart.api.container.IPartInfo;
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
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.BlockLogisticsSided;

public class BlockAbstractReader extends BlockLogisticsSided {

	public BlockAbstractReader(PL2Multiparts multipart) {
		super(multipart);
	}

	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getStateFromMeta(facing.ordinal()).withProperty(PL2Properties.HASDISPLAY, false);
	}

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		return EnumFaceSlot.fromFace(facing);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.VALUES[meta]).withProperty(PL2Properties.HASDISPLAY, false);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SonarProperties.ORIENTATION, PL2Properties.HASDISPLAY);
	}

	@Override
	public void onPartAdded(IPartInfo part, IPartInfo otherPart) {
		if (otherPart.getTile() instanceof IDisplay && part.getTile() instanceof TileAbstractReader) {
			onScreenChanged((TileAbstractReader) part.getTile(), (IDisplay) otherPart.getTile(), true);
		}
	}

	@Override
	public void onPartRemoved(IPartInfo part, IPartInfo otherPart) {
		if (otherPart.getTile() instanceof IDisplay && part.getTile() instanceof TileAbstractReader) {
			onScreenChanged((TileAbstractReader) part.getTile(), (IDisplay) otherPart.getTile(), false);
		}
	}

	@Deprecated
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		boolean hasDisplay = false;
		TileEntity tile = world.getTileEntity(pos);
		if(tile!=null && tile instanceof TileAbstractReader){
			hasDisplay = ((TileAbstractReader)tile).hasMonitor.getObject();
		}
		return state.withProperty(PL2Properties.HASDISPLAY, hasDisplay);
	}

	public void onScreenChanged(TileAbstractReader reader, IDisplay screen, boolean valid) {
		if (screen.getCableFace() == reader.getCableFace()) {
			reader.hasMonitor.setObject(valid);
			SonarMultipartHelper.sendMultipartUpdateSyncAround(reader, 128);
		}
	}

}
