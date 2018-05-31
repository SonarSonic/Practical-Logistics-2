package sonar.logistics.core.tiles.readers.fluids;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.containers.ContainerMultipartSync;
import sonar.core.inventory.slots.SlotList;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.core.tiles.displays.info.types.fluids.InfoNetworkFluid;
import sonar.logistics.core.tiles.readers.fluids.FluidReader.Modes;

import javax.annotation.Nonnull;

public class ContainerFluidReader extends ContainerMultipartSync {

	private static final int INV_START = 0, INV_END = INV_START + 26, HOTBAR_START = INV_END + 1, HOTBAR_END = HOTBAR_START + 8;
	public boolean stackMode = false;
	TileFluidReader part;

	public ContainerFluidReader(TileFluidReader tileFluidReader, EntityPlayer player) {
		super(tileFluidReader);
		this.part = tileFluidReader;
		addSlots(tileFluidReader, player, tileFluidReader.setting.getObject() == Modes.SELECTED);
	}

	public void addSlots(TileFluidReader handler, EntityPlayer player, boolean hasStack) {
		stackMode = hasStack;
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
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(par2);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (stackMode) {
				if (!part.getWorld().isRemote) {
					ItemStack copy = itemstack1.copy();
					FluidStack fluid = FluidUtil.getFluidContained(copy);
					if (fluid != null) {
						part.selected.setInfo(new InfoNetworkFluid(new StoredFluidStack(fluid)));
						part.sendByteBufPacket(1);
					}

				}
				if (!this.mergeItemStack(itemstack1.copy(), 0, INV_START, false)) {
					return ItemStack.EMPTY;
				}
			} else if (par2 < HOTBAR_START) {
				if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (par2 < HOTBAR_END + 1) {
				if (!this.mergeItemStack(itemstack1, INV_START, INV_END + 1, false)) {
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

			slot.onTake(par1EntityPlayer, itemstack1);
		}

		return itemstack;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			part.getListenerList().removeListener(player, true, ListenerType.OLD_GUI_LISTENER);
	}

	@Nonnull
    public ItemStack slotClick(int slotID, int drag, ClickType click, EntityPlayer player) {
		Slot targetSlot = slotID < 0 ? null : this.inventorySlots.get(slotID);
		if ((targetSlot instanceof SlotList)) {
			if (drag == 2) {
				targetSlot.putStack(null);
			} else {
                player.inventory.getItemStack();
                targetSlot.putStack(player.inventory.getItemStack().copy());
			}
			return player.inventory.getItemStack();
		}
		return super.slotClick(slotID, drag, click, player);
	}

	public SyncType[] getSyncTypes() {
		return new SyncType[] { SyncType.SPECIAL };
	}

}
