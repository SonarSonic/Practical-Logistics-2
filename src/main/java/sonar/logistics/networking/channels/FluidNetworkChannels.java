package sonar.logistics.networking.channels;

import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.networking.handlers.FluidNetworkHandler;

public class FluidNetworkChannels extends ListNetworkChannels<MonitoredFluidStack, FluidNetworkHandler> {

	public FluidNetworkChannels(ILogisticsNetwork network) {
		super(FluidNetworkHandler.INSTANCE, network);
	}
}
