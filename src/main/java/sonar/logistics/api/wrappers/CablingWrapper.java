package sonar.logistics.api.wrappers;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.EmptyNetworkCache;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.displays.ILargeDisplay;

public class CablingWrapper {

	public IDataCable getCableFromCoords(BlockCoords coords) {
		return null;
	}

	public ILogicTile getMultipart(BlockCoords coords, EnumFacing face) {
		return null;
	}

	public IInfoDisplay getDisplayScreen(BlockCoords coords, EnumFacing face) {
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
