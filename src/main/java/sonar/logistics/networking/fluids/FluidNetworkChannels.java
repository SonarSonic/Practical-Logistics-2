package sonar.logistics.networking.fluids;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.FluidChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.networking.common.ListNetworkChannels;

public class FluidNetworkChannels extends ListNetworkChannels<MonitoredFluidStack, FluidNetworkHandler> {

	public List<FluidStack> forRapidUpdate = Lists.newArrayList();

	public FluidNetworkChannels(ILogisticsNetwork network) {
		super(FluidNetworkHandler.INSTANCE, network);
	}

	public void updateChannel() {
		// as every reader is updated during the rapid update there is no need to update the queued channels/readers
		if (forRapidUpdate.isEmpty()) {
			super.updateChannel();
		} else {
			performRapidUpdates();// rapid updates do not do full lists
		}
	}

	public void performRapidUpdates() {
		updateAllChannels();
		for (IListReader reader : readers) {
			Pair<InfoUUID, AbstractChangeableList<MonitoredFluidStack>> updateList = handler.updateAndSendList(network, reader, channels, false);
			PacketHelper.sendRapidFluidUpdate(reader, updateList.a, (FluidChangeableList) updateList.b, forRapidUpdate);
		}
		forRapidUpdate.clear();
	}

	public void createRapidFluidUpdate(List<FluidStack> items) {
		newFluids: for (FluidStack stack : items) {
			for (FluidStack stored : forRapidUpdate) {
				if (StoredFluidStack.isEqualStack(stored, stack)) {
					continue newFluids;
				}
			}
			forRapidUpdate.add(stack.copy());
		}

	}
}
