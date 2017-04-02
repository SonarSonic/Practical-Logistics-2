package sonar.logistics.common.containers;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.connections.managers.EmitterManager;

public class ContainerStorageViewer extends Container {

	private static final int INV_START = 1, INV_END = INV_START + 26, HOTBAR_START = INV_END + 1, HOTBAR_END = HOTBAR_START + 8;
	public UUID uuid;
	public IDataEmitter emitter;
	public ItemStack lastStack = null;

	public EntityPlayer player;

	public ContainerStorageViewer(UUID uuid, EntityPlayer player) {
		super();
		this.uuid = uuid;
		this.player = player;
		if (!player.getEntityWorld().isRemote) {
			emitter = EmitterManager.getEmitter(uuid);
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 41 + j * 18, 174 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(player.inventory, i, 41 + i * 18, 232));
		}
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int id) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(id);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (id < 36) {
				if (!player.getEntityWorld().isRemote) {
					StoredItemStack stack = new StoredItemStack(itemstack1);
					if (lastStack != null && ItemStack.areItemStackTagsEqual(itemstack1, lastStack) && lastStack.isItemEqual(itemstack1)) {
						LogisticsAPI.getItemHelper().addItemsFromPlayer(stack, player, emitter.getNetwork(), ActionType.PERFORM);
					} else {
						StoredItemStack perform = LogisticsAPI.getItemHelper().addItems(stack, emitter.getNetwork(), ActionType.PERFORM);
						lastStack = itemstack1;

						itemstack1.stackSize = (int) (perform == null || perform.stored == 0 ? 0 : (perform.getStackSize()));
						player.inventory.markDirty();
					}
					this.detectAndSendChanges();
				}
			} else if (id < 27) {
				if (!this.mergeItemStack(itemstack1, 27, 36, false)) {
					return null;
				}
			} else if (id >= 27 && id < 36) {
				if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
					return null;
				}
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(player, itemstack1);
		}
		return itemstack;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote && emitter != null) {
			emitter.getViewersList().removeViewer(player, ViewerType.INFO);
		}
	}

	public SyncType[] getSyncTypes() {
		return new SyncType[] { SyncType.DEFAULT_SYNC };
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
}
