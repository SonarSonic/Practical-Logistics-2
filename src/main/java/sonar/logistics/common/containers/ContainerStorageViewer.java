package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.slots.SlotLimiter;
import sonar.logistics.PL2;
import sonar.logistics.PL2Items;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.networking.channels.ItemNetworkChannels;
import sonar.logistics.networking.channels.ListNetworkChannels;
import sonar.logistics.networking.connections.WirelessDataHandler;

public class ContainerStorageViewer extends Container {

	private static final int INV_START = 1, INV_END = INV_START + 26, HOTBAR_START = INV_END + 1, HOTBAR_END = HOTBAR_START + 8;
	public int identity;
	public IDataEmitter emitter;
	public ItemStack lastStack = null;

	public EntityPlayer player;

	public ContainerStorageViewer(int identity, EntityPlayer player) {
		super();
		this.identity = identity;
		this.player = player;
		if (!player.getEntityWorld().isRemote) {
			emitter = PL2.getWirelessManager().getDataEmitter(identity);
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new SlotLimiter(player.inventory, j + i * 9 + 9, 41 + j * 18, 174 + i * 18, PL2Items.wireless_storage_reader));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new SlotLimiter(player.inventory, i, 41 + i * 18, 232, PL2Items.wireless_storage_reader));
		}
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int id) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot) this.inventorySlots.get(id);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (id < 36) {
				if (!player.getEntityWorld().isRemote) {
					ILogisticsNetwork network = emitter.getNetwork();

					StoredItemStack stack = new StoredItemStack(itemstack1);
					if (lastStack != null && ItemStack.areItemStackTagsEqual(itemstack1, lastStack) && lastStack.isItemEqual(itemstack1)) {
						PL2API.getItemHelper().addItemsFromPlayer(stack, player, network, ActionType.PERFORM);
					} else {
						StoredItemStack perform = PL2API.getItemHelper().transferItems(network, stack, NodeTransferMode.ADD, ActionType.PERFORM, null);
						lastStack = itemstack1;
						itemstack1.setCount((int) (perform == null || perform.stored == 0 ? 0 : (perform.getStackSize())));
						player.inventory.markDirty();
					}
					ListNetworkChannels channels = network.getNetworkChannels(ItemNetworkChannels.class);
					if (channels != null) channels.sendLocalRapidUpdate(emitter, player);
					this.detectAndSendChanges();
				}
			} else if (id < 27) {
				if (!this.mergeItemStack(itemstack1, 27, 36, false)) {
					return ItemStack.EMPTY;
				}
			} else if (id >= 27 && id < 36) {
				if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (itemstack1.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
		}
		return itemstack;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote && emitter != null) {
			emitter.getListenerList().removeListener(player, true, ListenerType.LISTENER);
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
