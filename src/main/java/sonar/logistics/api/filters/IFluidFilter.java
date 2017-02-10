package sonar.logistics.api.filters;

import sonar.core.api.fluids.StoredFluidStack;

public interface IFluidFilter {

	public boolean canTransferFluid(StoredFluidStack stack);
	
}
