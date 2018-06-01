package sonar.logistics.core.tiles.displays.info.types.items;

import net.minecraft.item.ItemStack;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.StoredItemStack;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;

import javax.annotation.Nullable;

public class ItemChangeableList extends AbstractChangeableList<MonitoredItemStack> {

	public StorageSize sizing = new StorageSize(0, 0);

	public static ItemChangeableList newChangeableList() {
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

	/**generated the storage size list also*/
	public void add(ItemStack stack, long stored, long maxStored) {
		ItemCount found = find(stack);
		if (found == null) {
			values.add(createMonitoredValue(new MonitoredItemStack(new StoredItemStack(stack, stored))));
		} else {
			found.combine(stored);
		}
		sizing.add(stored);
		sizing.addToMax(maxStored);
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
