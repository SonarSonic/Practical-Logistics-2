package sonar.logistics.api.nodes;

import net.minecraft.util.EnumFacing;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;

public class NodeConnection {
	public BlockCoords coords;
	public EnumFacing face;
	public IConnectionNode source;
	public boolean isFiltered;

	public NodeConnection(IConnectionNode source, BlockCoords coords, EnumFacing face) {
		this.source = source;
		this.coords = coords;
		this.face = face;
		this.isFiltered = source instanceof IFilteredNode;
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
	
	public boolean canTransferFluid(StoredFluidStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredNode node = (IFilteredNode) source;
			if (!node.getSetting(TransferType.FLUID).canTransfer(mode) || !node.canTransferFluid(stack, mode)) {
				return false;
			}
		}		
		return true;		
	}
	
	public boolean canTransferItem(StoredItemStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredNode node = (IFilteredNode) source;
			if (!node.getSetting(TransferType.ITEMS).canTransfer(mode) || !node.canTransferItem(stack, mode)) {
				return false;
			}
		}		
		return true;		
	}
	
	public boolean canTransferEnergy(StoredEnergyStack stack, NodeTransferMode mode){
		if (isFiltered) {
			IFilteredNode node = (IFilteredNode) source;
			if (!node.getSetting(TransferType.ENERGY).canTransfer(mode) || !node.canTransferEnergy(stack, mode)) {
				return false;
			}
		}		
		return true;		
	}
	
}