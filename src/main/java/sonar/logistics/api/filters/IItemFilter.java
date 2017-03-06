package sonar.logistics.api.filters;

import sonar.core.api.inventories.StoredItemStack;

public interface IItemFilter {

	public boolean canTransferItem(StoredItemStack stack);
	
}
