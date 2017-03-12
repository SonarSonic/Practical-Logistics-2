package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGuide extends Container {
	public EntityPlayer player;

	public ContainerGuide(EntityPlayer player) {
		this.player = player;
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotID);
		return itemstack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}