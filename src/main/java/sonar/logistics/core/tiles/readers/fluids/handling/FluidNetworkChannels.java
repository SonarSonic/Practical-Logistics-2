package sonar.logistics.core.tiles.readers.fluids.handling;

import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.utils.Pair;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.base.channels.handling.ListNetworkChannels;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.types.fluids.FluidChangeableList;
import sonar.logistics.core.tiles.displays.info.types.fluids.InfoNetworkFluid;

import java.util.ArrayList;
import java.util.List;

public class FluidNetworkChannels extends ListNetworkChannels<InfoNetworkFluid, FluidNetworkHandler> {

	public List<FluidStack> forRapidUpdate = new ArrayList<>();

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
			Pair<InfoUUID, AbstractChangeableList<InfoNetworkFluid>> updateList = handler.updateAndSendList(network, reader, channels, false);
			InfoPacketHelper.sendRapidFluidUpdate(reader, updateList.a, (FluidChangeableList) updateList.b, forRapidUpdate);
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
