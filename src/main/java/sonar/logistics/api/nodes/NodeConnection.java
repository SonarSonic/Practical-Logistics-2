package sonar.logistics.api.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.IPriority;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.info.IMonitorInfo;

public abstract class NodeConnection<T extends IMonitorInfo> {
	public ILogicTile source;
	public boolean isFiltered;
	public int priority;

	public NodeConnection(ILogicTile source) {
		this.source = source;
		this.isFiltered = source instanceof ITransferFilteredTile;
		this.priority = source instanceof IPriority ? ((IPriority) source).getPriority() : 0;
	}
	
	public abstract T getChannel();

	public boolean canTransferFluid(NodeConnection connection, StoredFluidStack stack, NodeTransferMode mode) {
		if (isFiltered) {
			ITransferFilteredTile node = (ITransferFilteredTile) source;
			if (!node.getChannels().isMonitored(connection) && (!node.getTransferMode().matches(mode) || !node.getFilters().matches(stack, mode))) {
				return false;
			}
		}
		return true;
	}

	public boolean canTransferItem(NodeConnection connection, StoredItemStack stack, NodeTransferMode mode) {
		if (isFiltered) {
			ITransferFilteredTile node = (ITransferFilteredTile) source;
			if (!node.getChannels().isMonitored(connection) && (!node.getTransferMode().matches(mode) || !node.getFilters().matches(stack, mode))) {
				return false;
			}
		}
		return true;
	}

	public boolean canTransferEnergy(NodeConnection connection, StoredEnergyStack stack, NodeTransferMode mode) {
		if (isFiltered) {
			ITransferFilteredTile node = (ITransferFilteredTile) source;
			if (!node.getChannels().isMonitored(connection) && !(node.isTransferEnabled(TransferType.ENERGY))) {
				// if (!node.getSetting(TransferType.ENERGY).canTransfer(mode) || !node.canTransferEnergy(stack, mode)) {
				return false;
			}
		}
		return true;
	}
	
	public static ArrayList<? extends NodeConnection> sortConnections(ArrayList<? extends NodeConnection> connections){
		Collections.sort(connections, new Comparator<NodeConnection>() {
			public int compare(NodeConnection str1, NodeConnection str2) {
				return Integer.compare(str2.priority, str1.priority);
			}
		});
		return connections;
	}

}