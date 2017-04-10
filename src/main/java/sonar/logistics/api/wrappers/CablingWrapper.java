package sonar.logistics.api.wrappers;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.networks.EmptyNetworkCache;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;

public class CablingWrapper {

	public IDataCable getCableFromCoords(BlockCoords coords) {
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
		return EmptyNetworkCache.INSTANCE;
	}

	public ILogisticsNetwork getNetwork(int registryID) {
		return EmptyNetworkCache.INSTANCE;
	}
}
