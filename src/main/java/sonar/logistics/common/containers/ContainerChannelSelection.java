package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.tiles.IChannelledTile;
import sonar.logistics.api.viewers.ListenerType;

public class ContainerChannelSelection extends ContainerMultipartSync {

	public IChannelledTile tile;

	public ContainerChannelSelection(IChannelledTile tile) {
		super((SonarMultipart) tile);
		this.tile = tile;
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotID);
		return itemstack;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		tile.getListenerList().removeListener(player, ListenerType.CHANNEL);
	}

	public boolean syncInventory() {
		return false;
	}

	public SyncType[] getSyncTypes() {
		return new SyncType[] { SyncType.DEFAULT_SYNC };
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
}