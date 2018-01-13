package sonar.logistics.common.containers;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.logistics.common.multiparts2.nodes.TileArray;

public class ArraySlot extends Slot {

	TileArray part;

	public ArraySlot(TileArray tileArray, int index, int x, int y) {
		super(tileArray.inventory, index, x, y);
		this.part = tileArray;
	}

	public boolean isItemValid(ItemStack stack) {
		return part.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
	}
}