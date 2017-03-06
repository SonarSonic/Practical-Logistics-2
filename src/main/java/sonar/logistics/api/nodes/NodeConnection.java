package sonar.logistics.api.nodes;

import net.minecraft.util.EnumFacing;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.filters.IFilteredTile;

public class NodeConnection {
	public IConnectionNode source;
	public boolean isFiltered;

	public NodeConnection(IConnectionNode source) {
		this.source = source;
		this.isFiltered = source instanceof IFilteredTile;
	}
	
	public boolean canTransferFluid(BlockCoords coords, StoredFluidStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredTile node = (IFilteredTile) source;
			if ((node.getChannels().isEmpty() || node.getChannels().contains(coords)) && (!node.getTransferMode().matches(mode) || !node.getFilters().matches(stack, mode))) {
				return false;
			}
		}		
		return true;		
	}
	
	public boolean canTransferItem(BlockCoords coords, StoredItemStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredTile node = (IFilteredTile) source;
			if ((node.getChannels().isEmpty() || node.getChannels().contains(coords)) && (!node.getTransferMode().matches(mode) || !node.getFilters().matches(stack, mode))) {
				return false;
			}
		}		
		return true;		
	}
	
	public boolean canTransferEnergy(BlockCoords coords, StoredEnergyStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredTile node = (IFilteredTile) source;
			if((node.getChannels().isEmpty() || node.getChannels().contains(coords)) && (node.isTransferEnabled(TransferType.ENERGY))){
			//if (!node.getSetting(TransferType.ENERGY).canTransfer(mode) || !node.canTransferEnergy(stack, mode)) {
				return false;
			}
		}		
		return true;		
	}
	
}