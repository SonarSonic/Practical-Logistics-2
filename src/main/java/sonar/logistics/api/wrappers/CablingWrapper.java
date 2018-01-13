package sonar.logistics.api.wrappers;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.networks.EmptyLogisticsNetwork;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.common.multiparts2.cables.TileDataCable;

public class CablingWrapper {

	public TileDataCable getCable(IBlockAccess world, BlockPos pos) {
		return null;
	}

	public INetworkTile getMultipart(BlockCoords coords, EnumFacing face) {
		return null;
	}

	public IDisplay getDisplayScreen(BlockCoords coords, EnumFacing face) {
		return null;
	}
	
	public ILargeDisplay getDisplayScreen(BlockCoords coords, int registryID) {
		return null;
	}

	public ILogisticsNetwork getNetwork(TileEntity tile, EnumFacing dir) {
		return EmptyLogisticsNetwork.INSTANCE;
	}

	public ILogisticsNetwork getNetwork(int registryID) {
		return EmptyLogisticsNetwork.INSTANCE;
	}
}
