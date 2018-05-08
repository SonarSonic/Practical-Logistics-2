package sonar.logistics.core.tiles.readers.energy.handling;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.logistics.PL2Config;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListChannels;
import sonar.logistics.api.core.tiles.readers.channels.ITileMonitorHandler;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.handling.ListNetworkHandler;
import sonar.logistics.base.utils.LogisticsHelper;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.energy.MonitoredEnergyStack;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;

import java.util.List;

public class EnergyNetworkHandler<C extends INetworkListChannels> extends ListNetworkHandler<MonitoredEnergyStack, InfoChangeableList> implements ITileMonitorHandler<MonitoredEnergyStack, InfoChangeableList, INetworkListChannels> {

	public static EnergyNetworkHandler INSTANCE = new EnergyNetworkHandler();

	@Override
	public Class<? extends INetworkListChannels> getChannelsType() {
		return EnergyNetworkChannels.class;
	}

	@Override
	public InfoChangeableList updateInfo(INetworkListChannels channels, InfoChangeableList list, BlockConnection connection) {
		List<ISonarEnergyHandler> providers = SonarCore.energyHandlers;
		StoredEnergyStack maxEnergy = null;
		for (ISonarEnergyHandler provider : providers) {
			TileEntity tile = connection.coords.getTileEntity();
			if (tile != null && provider.canProvideEnergy(tile, connection.face)) {
				StoredEnergyStack info = provider.getEnergy(new StoredEnergyStack(provider.getProvidedType()), tile, connection.face);
				if (info != null) {
                    maxEnergy = info;
                    break;
				}
			}
		}
		if (maxEnergy != null) {
			TileEntity tile = connection.coords.getTileEntity();
			MonitoredEnergyStack coords = new MonitoredEnergyStack(maxEnergy, new MonitoredBlockCoords(connection.coords, LogisticsHelper.getCoordItem(connection.coords, connection.coords.getWorld())));
			list.add(coords);
		}
		return list;
	}

	@Override
	public int updateRate() {
		return PL2Config.energyUpdate;
	}

	@Override
	public InfoChangeableList newChangeableList() {
		return new InfoChangeableList();
	}
}
