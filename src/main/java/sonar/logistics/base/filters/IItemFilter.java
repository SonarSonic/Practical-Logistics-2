package sonar.logistics.base.filters;

import net.minecraft.item.ItemStack;

public interface IItemFilter {

	boolean canTransferItem(ItemStack stack);
	
}
