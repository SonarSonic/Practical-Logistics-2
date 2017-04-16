package sonar.logistics.connections.channels;

import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.connections.handlers.FluidNetworkHandler;
import sonar.logistics.info.types.MonitoredFluidStack;

public class FluidNetworkChannels extends ListNetworkChannels<MonitoredFluidStack, FluidNetworkHandler> {

	public FluidNetworkChannels(ILogisticsNetwork network) {
		super(FluidNetworkHandler.INSTANCE, network);
	}
}
