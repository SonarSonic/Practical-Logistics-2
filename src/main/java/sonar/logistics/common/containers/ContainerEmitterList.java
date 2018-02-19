package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import sonar.logistics.networking.cabling.WirelessDataManager;

public class ContainerEmitterList extends Container {
	public EntityPlayer player;

	public ContainerEmitterList(EntityPlayer player) {
		this.player = player;
	}
	
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			WirelessDataManager.instance().removeViewer(player);
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}