package sonar.logistics.common.multiparts.nodes;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.common.multiparts.BlockLogisticsSided;

public class BlockTransferNode extends BlockLogisticsSided {

	public static final PropertyEnum<NodeTransferMode> TRANSFER = PropertyEnum.<NodeTransferMode>create("transfer", NodeTransferMode.class);

	public BlockTransferNode() {
		super(PL2Multiparts.TRANSFER_NODE);
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		NodeTransferMode mode = NodeTransferMode.ADD;
		TileEntity tile = world.getTileEntity(pos);
		if(tile!=null && tile instanceof TileTransferNode){
			mode = ((TileTransferNode)tile).getTransferMode();
		}
		return state.withProperty(TRANSFER, mode);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SonarProperties.ORIENTATION, TRANSFER });
	}
}
