package sonar.logistics.api.nodes;

import net.minecraft.util.EnumFacing;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.filters.IFilteredTile;

public class NodeConnection {
	public BlockCoords coords;
	public EnumFacing face;
	public IConnectionNode source;
	public boolean isFiltered;

	public NodeConnection(IConnectionNode source, BlockCoords coords, EnumFacing face) {
		this.source = source;
		this.coords = coords;
		this.face = face;
		this.isFiltered = source instanceof IFilteredTile;
	}

	public int hashCode() {
		return coords.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof NodeConnection) {
			return ((NodeConnection) obj).coords.equals(coords);
		}
		return false;
	}
	
	public boolean canTransferFluid(BlockCoords source, StoredFluidStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredTile node = (IFilteredTile) source;
			if ((node.getChannels().isEmpty() || node.getChannels().contains(source)) && (!node.getTransferMode().matches(mode) || !node.getFilters().matches(stack, mode))) {
				return false;
			}
		}		
		return true;		
	}
	
	public boolean canTransferItem(BlockCoords source, StoredItemStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredTile node = (IFilteredTile) source;
			if ((node.getChannels().isEmpty() || node.getChannels().contains(source)) && (!node.getTransferMode().matches(mode) || !node.getFilters().matches(stack, mode))) {
				return false;
			}
		}		
		return true;		
	}
	
	public boolean canTransferEnergy(BlockCoords source, StoredEnergyStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredTile node = (IFilteredTile) source;
			if((node.getChannels().isEmpty() || node.getChannels().contains(source)) && (node.isTransferEnabled(TransferType.ENERGY))){
			//if (!node.getSetting(TransferType.ENERGY).canTransfer(mode) || !node.canTransferEnergy(stack, mode)) {
				return false;
			}
		}		
		return true;		
	}
	
}