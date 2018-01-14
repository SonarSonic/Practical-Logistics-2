package sonar.logistics.networking.channels;

import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.info.types.MonitoredEnergyStack;
import sonar.logistics.networking.handlers.EnergyNetworkHandler;

public class EnergyNetworkChannels extends ListNetworkChannels<MonitoredEnergyStack, EnergyNetworkHandler> {

	public EnergyNetworkChannels(ILogisticsNetwork network) {
		super(EnergyNetworkHandler.INSTANCE, network);
	}
}
