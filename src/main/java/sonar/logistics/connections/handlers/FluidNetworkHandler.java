package sonar.logistics.connections.handlers;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.StorageSize;
import sonar.core.api.fluids.ISonarFluidHandler;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.logistics.PL2Config;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.common.multiparts2.wireless.TileDataEmitter;
import sonar.logistics.connections.channels.FluidNetworkChannels;
import sonar.logistics.info.types.MonitoredFluidStack;

public class FluidNetworkHandler extends ListNetworkHandler<MonitoredFluidStack> implements ITileMonitorHandler<MonitoredFluidStack,INetworkListChannels> {
	
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
	public MonitoredList<MonitoredFluidStack> updateInfo(INetworkListChannels channels, MonitoredList<MonitoredFluidStack> newList, MonitoredList<MonitoredFluidStack> previousList, BlockConnection connection) {
		List<ISonarFluidHandler> providers = SonarCore.fluidHandlers;
		for (ISonarFluidHandler provider : providers) {
			TileEntity fluidTile = connection.coords.getTileEntity();
			if (fluidTile != null && provider.canHandleFluids(fluidTile, connection.face)) {
				List<StoredFluidStack> info = Lists.newArrayList();
				StorageSize size = provider.getFluids(info, fluidTile, connection.face);
				newList.sizing.add(size);
				for (StoredFluidStack fluid : info) {
					newList.addInfoToList(new MonitoredFluidStack(fluid), previousList);
				}
				break;
			}
		}
		return newList;
	}

	@Override
	public int updateRate() {
		return PL2Config.fluidUpdate;
	}

}
