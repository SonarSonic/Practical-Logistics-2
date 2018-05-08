package sonar.logistics.core.tiles.displays.info.types.items;

import net.minecraft.item.ItemStack;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.lists.EnumListChange;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;

@ASMMonitoredValue(id = ItemCount.id, modid = PL2Constants.MODID)
public class ItemCount implements IMonitoredValue<MonitoredItemStack> {

	public static final String id = "item_count";
	public MonitoredItemStack item;
	public long old = 0;
	public boolean isNew;

	public ItemCount(MonitoredItemStack stack) {
		reset(stack);
		this.isNew = true;
	}

	@Override
	public EnumListChange getChange() {
		if (isNew) {
			return EnumListChange.NEW_VALUE;
		}
		return EnumListChange.getChange(item.getStoredStack().stored, old);
	}

	@Override
	public void resetChange() {
		old = item.getStoredStack().stored;
		item.getStoredStack().stored = 0;
		isNew = false;
	}

	@Override
	public void combine(MonitoredItemStack combine) {
		item.getStoredStack().stored += combine.getStoredStack().stored;
	}

	public void combine(long stored) {
		item.getStoredStack().stored += stored;
	}

	@Override
	public boolean canCombine(MonitoredItemStack combine) {
		return item.getStoredStack().equalStack(combine.getStoredStack().item);
	}

	public boolean canCombine(ItemStack combine) {
		return item.getStoredStack().equalStack(combine);
	}

	@Override
	public boolean isValid(Object info) {
		return info instanceof MonitoredItemStack;
	}

	@Override
	public MonitoredItemStack getSaveableInfo() {
		return item;
	}

	@Override
	public void reset(MonitoredItemStack fullInfo) {
		this.item = fullInfo.copy();
	}

	@Override
	public void setNew() {
		this.isNew = true;
	}

	@Override
	public boolean shouldDelete(EnumListChange change) {
		return change.shouldDelete() || item ==null || item.getStored() == 0; //rapid updates cause items counts to be "new" even if they have been completely removed
	}

}