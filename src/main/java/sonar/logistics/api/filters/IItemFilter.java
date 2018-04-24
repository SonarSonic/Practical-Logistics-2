package sonar.logistics.api.filters;

import sonar.core.api.inventories.StoredItemStack;

public interface IItemFilter {

	boolean canTransferItem(StoredItemStack stack);
	
}
