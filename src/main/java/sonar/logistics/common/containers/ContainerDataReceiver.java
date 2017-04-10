package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.common.multiparts.DataReceiverPart;
import sonar.logistics.managers.WirelessManager;

public class ContainerDataReceiver extends ContainerMultipartSync {

	public ContainerDataReceiver(DataReceiverPart entity) {
		super(entity);
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotID);
		return itemstack;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		WirelessManager.removeViewer(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
}
