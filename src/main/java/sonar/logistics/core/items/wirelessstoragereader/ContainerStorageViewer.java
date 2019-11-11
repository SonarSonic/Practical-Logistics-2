package sonar.logistics.core.items.wirelessstoragereader;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.handlers.inventories.slots.SlotLimiter;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Items;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.wireless.emitters.IDataEmitter;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.core.tiles.readers.items.handling.ItemHelper;
import sonar.logistics.core.tiles.wireless.handling.WirelessDataManager;

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
			emitter = WirelessDataManager.instance().getEmitter(identity);
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
		Slot slot = this.inventorySlots.get(id);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			lastStack = itemstack1;
			if (id < 36) {
				if (!player.getEntityWorld().isRemote) {
					ILogisticsNetwork network = emitter.getNetwork();

					StoredItemStack stack = new StoredItemStack(itemstack1);
					if (lastStack != null && ItemStack.areItemStackTagsEqual(itemstack1, lastStack) && lastStack.isItemEqual(itemstack1)) {
						ItemHelper.transferPlayerInventoryToNetwork(player, network, s -> StoredItemStack.isEqualStack(lastStack, s), lastStack.getCount());
					} else {
						itemstack1 = ItemHelper.insertItemStack(network, itemstack1, 64);
					}
					/* TODO MAKE SMARTER VERSION OF THIS
					ListNetworkChannels channels = network.getNetworkChannels(ItemNetworkChannels.class);

					if (channels != null) channels.sendLocalRapidUpdate(emitter, player);
					*/
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
			emitter.getListenerList().removeListener(player, true, ListenerType.OLD_GUI_LISTENER);
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
