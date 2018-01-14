package sonar.logistics.api.lists.types;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.StoredItemStack;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.values.ItemCount;
import sonar.logistics.info.types.MonitoredItemStack;

public class ItemChangeableList extends AbstractChangeableList<MonitoredItemStack> {

	public StorageSize sizing = new StorageSize(0, 0);// FIXME

	@Override
	public ItemCount createMonitoredValue(MonitoredItemStack obj) {
		return new ItemCount(obj);
	}
	
	public void add(StoredItemStack stack) {		
		ItemCount found = find(stack.item, stack.stored);
		if (found == null) {
			list.add(createMonitoredValue(new MonitoredItemStack(stack)));
		} else {
			found.combine(stack.item, stack.stored);
		}
	}

	@Nullable
	public ItemCount find(ItemStack obj, long stored) {
		for (IMonitoredValue<MonitoredItemStack> value : list) {
			ItemCount count = (ItemCount) value;
			if (count.canCombine(obj, stored)) {
				return count;
			}
		}
		return null;
	}
	
	public long getItemCount(ItemStack stack){
		for(IMonitoredValue<MonitoredItemStack> value : getList()){
			if(value instanceof ItemCount){
				ItemCount count = (ItemCount) value;
				if(count.item.getStoredStack().equalStack(stack)){
					return count.item.getStored();
				}
			}
		}		
		return 0;		
	}
}
