package sonar.logistics.api.tiles.nodes;

import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.IPriority;

import java.util.List;

public abstract class NodeConnection<T extends IInfo> {
	
	
	public INetworkTile source;
	public boolean isFiltered;
	public int priority;

	public NodeConnection(INetworkTile source) {
		this.source = source;
		this.isFiltered = source instanceof ITransferFilteredTile;
		this.priority = source instanceof IPriority ? ((IPriority) source).getPriority() : 0;
	}
	
	public abstract T getChannel();
	
	public abstract NodeConnectionType getType();

	public boolean canTransferFluid(NodeConnection connection, StoredFluidStack stack, NodeTransferMode mode) {
		if (isFiltered) {
			ITransferFilteredTile node = (ITransferFilteredTile) source;
            return node.getChannels().isMonitored(connection) || (node.getTransferMode().matches(mode) && node.getFilters().matches(stack, mode));
		}
		return true;
	}

	public boolean canTransferItem(NodeConnection connection, StoredItemStack stack, NodeTransferMode mode) {
		if (isFiltered) {
			ITransferFilteredTile node = (ITransferFilteredTile) source;
            return node.getChannels().isMonitored(connection) || (node.getTransferMode().matches(mode) && node.getFilters().matches(stack, mode));
		}
		return true;
	}

	public boolean canTransferEnergy(NodeConnection connection, StoredEnergyStack stack, NodeTransferMode mode) {
		if (isFiltered) {
			ITransferFilteredTile node = (ITransferFilteredTile) source;
            return node.getChannels().isMonitored(connection) || node.isTransferEnabled(TransferType.ENERGY);
		}
		return true;
	}
	
	public static List<NodeConnection> sortConnections(List<NodeConnection> connections){
		connections.sort((str1, str2) -> Integer.compare(str2.priority, str1.priority));
		return connections;
	}

}