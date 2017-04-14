package sonar.logistics.common.containers;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.logistics.common.multiparts.nodes.ArrayPart;

public class ArraySlot extends Slot {

	ArrayPart part;

	public ArraySlot(ArrayPart part, int index, int x, int y) {
		super(part.inventory, index, x, y);
		this.part = part;
	}

	public boolean isItemValid(ItemStack stack) {
		return part.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
	}
}