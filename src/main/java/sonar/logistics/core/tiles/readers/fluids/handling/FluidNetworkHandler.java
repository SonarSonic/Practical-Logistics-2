package sonar.logistics.core.tiles.readers.fluids.handling;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.StorageSize;
import sonar.core.api.fluids.ISonarFluidHandler;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.logistics.PL2Config;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListChannels;
import sonar.logistics.api.core.tiles.readers.channels.ITileMonitorHandler;
import sonar.logistics.api.core.tiles.wireless.emitters.IDataEmitter;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.handling.ListNetworkHandler;
import sonar.logistics.core.tiles.displays.info.types.fluids.FluidChangeableList;
import sonar.logistics.core.tiles.displays.info.types.fluids.InfoNetworkFluid;
import sonar.logistics.core.tiles.wireless.emitters.TileDataEmitter;

import java.util.ArrayList;
import java.util.List;

public class FluidNetworkHandler extends ListNetworkHandler<InfoNetworkFluid, FluidChangeableList> implements ITileMonitorHandler<InfoNetworkFluid, FluidChangeableList,INetworkListChannels> {
	
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
					list.add(new InfoNetworkFluid(fluid));
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
