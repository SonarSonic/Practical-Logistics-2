package sonar.logistics.common.multiparts.holographic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.PL2Properties;

import javax.annotation.Nonnull;

public class BlockAdvancedHolographicDisplay extends BlockAbstractHolographicDisplay {

	public BlockAdvancedHolographicDisplay() {
		super(PL2Multiparts.ADVANCED_HOLOGRAPHIC_DISPLAY);
	}

	@Nonnull
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return PL2Properties.getStandardBox(getOrientation(state), getMultipart());
	}

	@Nonnull
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
}
