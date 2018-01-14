package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.inventory.slots.SlotList;
import sonar.logistics.api.tiles.readers.FluidReader.Modes;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.readers.TileFluidReader;
import sonar.logistics.info.types.MonitoredFluidStack;

public class ContainerFluidReader extends ContainerMultipartSync {

	private static final int INV_START = 1, INV_END = INV_START + 26, HOTBAR_START = INV_END + 1, HOTBAR_END = HOTBAR_START + 8;
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
		Slot slot = (Slot) this.inventorySlots.get(par2);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (stackMode && par2 >= INV_START) {
				if (!part.getWorld().isRemote) {
					ItemStack copy = itemstack1.copy();
					boolean hasCapability = copy.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
					if(hasCapability){
						IFluidHandlerItem fluidItem = copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
						ItemStack container = fluidItem.getContainer();
						if(container!=null){
							for(int i=0;i<fluidItem.getTankProperties().length;i++){
								IFluidTankProperties tank = fluidItem.getTankProperties()[i];
								if(tank.getContents()!=null && tank.getContents().amount!=0){
									part.selected.setInfo(new MonitoredFluidStack(new StoredFluidStack(tank.getContents())));
								}
							}
						}
					}
					// FIXME
					/*
					if (copy != null && copy.getItem() instanceof IFluidHandlerItem) {
						IFluidContainerItem container = (IFluidContainerItem) copy.getItem();
						FluidStack stack = container.getFluid(copy);
						if (stack != null) {
							// part.current = stack;
						}
					} else if (copy != null) {
						FluidStack fluid = FluidContainerUtils.getFluidForFilledItem(copy);
						if (fluid != null) {
							// part.current = fluid;
						}
					}
					*/
				}
				if (!this.mergeItemStack(itemstack1.copy(), 0, INV_START, false)) {
					return ItemStack.EMPTY;
				}
			} else if (par2 >= INV_START && par2 < HOTBAR_START) {
				if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (par2 >= HOTBAR_START && par2 < HOTBAR_END + 1) {
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
			part.getListenerList().removeListener(player, true, ListenerType.INFO);
	}

	public ItemStack slotClick(int slotID, int drag, ClickType click, EntityPlayer player) {
		Slot targetSlot = slotID < 0 ? null : (Slot) this.inventorySlots.get(slotID);
		if ((targetSlot instanceof SlotList)) {
			if (drag == 2) {
				targetSlot.putStack(null);
			} else {
				targetSlot.putStack(player.inventory.getItemStack() == null ? null : player.inventory.getItemStack().copy());
			}
			return player.inventory.getItemStack();
		}
		return super.slotClick(slotID, drag, click, player);
	}

	public SyncType[] getSyncTypes() {
		return new SyncType[] { SyncType.SPECIAL };
	}

}
