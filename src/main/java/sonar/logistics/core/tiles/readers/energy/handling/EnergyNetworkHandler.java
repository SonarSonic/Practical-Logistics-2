package sonar.logistics.core.tiles.readers.energy.handling;

import net.minecraft.tileentity.TileEntity;
import sonar.core.api.energy.ITileEnergyHandler;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.handlers.energy.EnergyTransferHandler;
import sonar.logistics.PL2Config;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListChannels;
import sonar.logistics.api.core.tiles.readers.channels.ITileMonitorHandler;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.handling.ListNetworkHandler;
import sonar.logistics.base.utils.LogisticsHelper;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.energy.MonitoredEnergyStack;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;

public class EnergyNetworkHandler<C extends INetworkListChannels> extends ListNetworkHandler<MonitoredEnergyStack, InfoChangeableList> implements ITileMonitorHandler<MonitoredEnergyStack, InfoChangeableList, INetworkListChannels> {

	public static EnergyNetworkHandler INSTANCE = new EnergyNetworkHandler();

	@Override
	public Class<? extends INetworkListChannels> getChannelsType() {
		return EnergyNetworkChannels.class;
	}

	@Override
	public InfoChangeableList updateInfo(INetworkListChannels channels, InfoChangeableList list, BlockConnection connection) {
		TileEntity tile = connection.coords.getTileEntity();
		if(tile != null) {
			ITileEnergyHandler handler = EnergyTransferHandler.INSTANCE_SC.getTileHandler(tile, connection.face);
			if(handler.canReadEnergy(tile, connection.face)) {
				StoredEnergyStack energyStack = new StoredEnergyStack(handler.getEnergyType());
				energyStack.setStorageValues(handler.getStored(tile, connection.face), handler.getCapacity(tile, connection.face));
				MonitoredEnergyStack coords = new MonitoredEnergyStack(energyStack, new MonitoredBlockCoords(connection.coords, LogisticsHelper.getCoordItem(connection.coords, connection.coords.getWorld())));
				list.add(coords);
			}
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
