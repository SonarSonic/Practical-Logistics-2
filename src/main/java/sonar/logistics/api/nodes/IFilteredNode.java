package sonar.logistics.api.nodes;

import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;

public interface IFilteredNode extends IConnectionNode {

	public NodeSetting getSetting(TransferType type);
	
	public boolean canTransferItem(StoredItemStack stack, NodeTransferMode type);
	
	public boolean canTransferFluid(StoredFluidStack stack, NodeTransferMode type);
	
	public boolean canTransferEnergy(StoredEnergyStack stack, NodeTransferMode type);
	
	//gas
}
