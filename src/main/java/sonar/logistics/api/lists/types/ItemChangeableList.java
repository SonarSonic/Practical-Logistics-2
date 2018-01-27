package sonar.logistics.api.lists.types;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.StoredItemStack;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.values.ItemCount;
import sonar.logistics.info.types.MonitoredItemStack;

public class ItemChangeableList extends AbstractChangeableList<MonitoredItemStack> {

	public StorageSize sizing = new StorageSize(0, 0);

	public static final ItemChangeableList newChangeableList() {
		return new ItemChangeableList();
	}

	@Override
	public ItemCount createMonitoredValue(MonitoredItemStack obj) {
		return new ItemCount(obj);
	}

	public void saveStates() {
		super.saveStates();
		sizing = new StorageSize(0, 0);
	}

	public void add(StoredItemStack stack) {
		ItemCount found = find(stack.item);
		if (found == null) {
			values.add(createMonitoredValue(new MonitoredItemStack(stack)));
		} else {
			found.combine(stack.stored);
		}
	}

	@Nullable
	public ItemCount find(ItemStack obj) {
		for (IMonitoredValue<MonitoredItemStack> value : values) {
			ItemCount count = (ItemCount) value;
			if (count.canCombine(obj)) {
				return count;
			}
		}
		return null;
	}

	public long getItemCount(ItemStack stack) {
		for (IMonitoredValue<MonitoredItemStack> value : getList()) {
			if (value instanceof ItemCount) {
				ItemCount count = (ItemCount) value;
				if (count.item.getStoredStack().equalStack(stack)) {
					return count.item.getStored();
				}
			}
		}
		return 0;
	}
}
