package sonar.logistics.networking.energy;

import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.info.types.MonitoredEnergyStack;
import sonar.logistics.networking.common.ListNetworkChannels;

public class EnergyNetworkChannels extends ListNetworkChannels<MonitoredEnergyStack, EnergyNetworkHandler> {

	public EnergyNetworkChannels(ILogisticsNetwork network) {
		super(EnergyNetworkHandler.INSTANCE, network);
	}
}
