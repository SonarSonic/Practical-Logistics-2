package sonar.logistics.networking.fluids;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.StorageSize;
import sonar.core.api.fluids.ISonarFluidHandler;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.logistics.PL2Config;
import sonar.logistics.api.lists.types.FluidChangeableList;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.common.multiparts.wireless.TileDataEmitter;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.networking.common.ListNetworkHandler;

public class FluidNetworkHandler extends ListNetworkHandler<MonitoredFluidStack, FluidChangeableList> implements ITileMonitorHandler<MonitoredFluidStack, FluidChangeableList,INetworkListChannels> {
	
	public static FluidNetworkHandler INSTANCE = new FluidNetworkHandler();
	
	@Override
	public Class<? extends INetworkListChannels> getChannelsType() {
		return FluidNetworkChannels.class;
	}
	
	public int getReaderID(IListReader reader){
		if(reader instanceof IDataEmitter){
			return TileDataEmitter.STATIC_FLUID_ID;
		}		
		return 0;
	}

	@Override
	public FluidChangeableList updateInfo(INetworkListChannels channels, FluidChangeableList list, BlockConnection connection) {
		List<ISonarFluidHandler> providers = SonarCore.fluidHandlers;
		for (ISonarFluidHandler provider : providers) {
			TileEntity fluidTile = connection.coords.getTileEntity();
			if (fluidTile != null && provider.canHandleFluids(fluidTile, connection.face)) {
				List<StoredFluidStack> info = new ArrayList<>();
				StorageSize size = provider.getFluids(info, fluidTile, connection.face);
				list.sizing.add(size);
				for (StoredFluidStack fluid : info) {
					list.add(new MonitoredFluidStack(fluid));
				}
				break;
			}
		}
		return list;
	}

	@Override
	public int updateRate() {
		return PL2Config.fluidUpdate;
	}

	@Override
	public FluidChangeableList newChangeableList() {
		return new FluidChangeableList();
	}

}
