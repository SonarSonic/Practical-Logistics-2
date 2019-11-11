package sonar.logistics.core.tiles.readers.items;
/*
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.api.IFlexibleContainer;
import sonar.core.handlers.inventories.containers.ContainerMultipartSync;
import sonar.core.handlers.inventories.slots.SlotList;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.core.tiles.readers.items.InventoryReader.Modes;
import sonar.logistics.core.tiles.readers.items.handling.ItemHelper;

import javax.annotation.Nonnull;

public class ContainerInventoryReader extends ContainerMultipartSync implements IFlexibleContainer<InventoryReader.Modes> {

	private static final int INV_START = 1, INV_END = INV_START + 26, HOTBAR_START = INV_END + 1, HOTBAR_END = HOTBAR_START + 8;
	public boolean stackMode = false;
	public ItemStack lastStack = null;
	public TileInventoryReader part;
	public EntityPlayer player;

	public ContainerInventoryReader(TileInventoryReader tileInventoryReader, EntityPlayer player) {
		super(tileInventoryReader);
		this.part = tileInventoryReader;
		this.player = player;
		refreshState();
	}

	@Override
	public void refreshState() {
		InventoryReader.Modes state = getCurrentState();
		stackMode = state == Modes.STACK;
		this.inventoryItemStacks.clear();
		this.inventorySlots.clear();
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 41 + j * 18, 174 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(player.inventory, i, 41 + i * 18, 232));
		}
		if (stackMode)
			addSlotToContainer(new SlotList(part.inventory.getWrapperInventory(), 0, 63, 9));
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int id) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(id);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			lastStack = itemstack1;
			if (id < 36) {
				if (!part.getWorld().isRemote) {
					ILogisticsNetwork network = part.getNetwork();
					itemstack1 = ItemHelper.insertItemStack(network, itemstack1, itemstack1.getCount());
				}
			} else if (!this.mergeItemStack(itemstack1, 0, 36, false)) {
				return ItemStack.EMPTY;
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


		/* TODO make smarted version of this
		ItemNetworkChannels channels = part.getNetwork().getNetworkChannels(ItemNetworkChannels.class);
		if (channels != null)
			channels.sendLocalRapidUpdate(part, player);
		this.detectAndSendChanges();


import javax.annotation.Nonnull;return itemstack;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			part.getListenerList().removeListener(player, true, ListenerType.OLD_GUI_LISTENER);
	}

	@Nonnull
    public ItemStack slotClick(int slotID, int drag, ClickType click, EntityPlayer player) {
		if (slotID < this.inventorySlots.size()) {
			Slot targetSlot = slotID < 0 ? null : this.inventorySlots.get(slotID);
			if ((targetSlot instanceof SlotList)) {
				targetSlot.putStack(drag == 2 ? null : player.inventory.getItemStack().copy());
				return player.inventory.getItemStack();
			}
			return super.slotClick(slotID, drag, click, player);
		}
		return null;
	}
	
	@Override
	public InventoryReader.Modes getCurrentState() {
		return part.setting.getObject();
	}
}
*/