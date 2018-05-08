package sonar.logistics.core.tiles.readers.energy.handling;

import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.base.channels.handling.ListNetworkChannels;
import sonar.logistics.core.tiles.displays.info.types.energy.MonitoredEnergyStack;

public class EnergyNetworkChannels extends ListNetworkChannels<MonitoredEnergyStack, EnergyNetworkHandler> {

	public EnergyNetworkChannels(ILogisticsNetwork network) {
		super(EnergyNetworkHandler.INSTANCE, network);
	}
}
