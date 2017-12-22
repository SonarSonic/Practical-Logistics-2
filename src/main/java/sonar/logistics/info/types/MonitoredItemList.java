package sonar.logistics.info.types;

import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.ItemStackHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.InfoUUID;
/*
@LogicInfoType(id = MonitoredItemStack.id, modid = PL2Constants.MODID)
public class MonitoredItemList extends NewMonitoredList<ItemStack> {

	public static final String id = "itemlist";

	public MonitoredItemList(int hashcode) {
		super(hashcode);
	}

	public void addStack(StoredItemStack stack) {
		NewMonitoredListHandler.addStack(this, stack.getItemStack(), stack.stored);
	}

	public void addStack(MonitoredItemStack stack) {
		NewMonitoredListHandler.addStack(this, stack.getItemStack(), stack.getStored());
	}

	public void removeStack(StoredItemStack stack) {
		NewMonitoredListHandler.removeStack(this, stack.getItemStack(), stack.stored);
	}

	public void removeStack(MonitoredItemStack stack) {
		NewMonitoredListHandler.removeStack(this, stack.getItemStack(), stack.getStored());
	}

	@Override
	public boolean equal(ItemStack key1, ItemStack key2) {
		return ItemStackHelper.equalStacksRegular(key1, key2);
	}

	@Override
	public ItemStack copy(ItemStack copy) {
		return copy.copy();
	}

	@Override
	public String getID() {
		return id;
	}

}
*/