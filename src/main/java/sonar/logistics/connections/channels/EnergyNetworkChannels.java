package sonar.logistics.connections.channels;

import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.connections.handlers.EnergyNetworkHandler;
import sonar.logistics.info.types.MonitoredEnergyStack;

public class EnergyNetworkChannels extends ListNetworkChannels<MonitoredEnergyStack, EnergyNetworkHandler> {

	public EnergyNetworkChannels(ILogisticsNetwork network) {
		super(EnergyNetworkHandler.INSTANCE, network);
	}
}
