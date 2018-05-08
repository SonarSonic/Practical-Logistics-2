package sonar.logistics.core.tiles.misc.hammer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.inventory.ContainerSync;
import sonar.core.inventory.TransferSlotsManager;
import sonar.core.inventory.slots.SlotBlockedInventory;

import javax.annotation.Nonnull;

public class ContainerHammer extends ContainerSync {

	private TileEntityHammer entity;

	public static TransferSlotsManager<TileEntityHammer> transfer = new TransferSlotsManager() {
		{
			addTransferSlot(new TransferSlots<TileEntityHammer>(TransferType.TILE_INV, 1) {
				@Override
				public boolean canInsert(EntityPlayer player, TileEntityHammer inv, Slot slot, int pos, int slotID, ItemStack stack) {
					return inv.isItemValidForSlot(0, stack);
				}
			});
			addTransferSlot(new DisabledSlots<TileEntityHammer>(TransferType.TILE_INV, 1));
			addPlayerInventory();
		}
	};

	public ContainerHammer(EntityPlayer player, TileEntityHammer entity) {
		super(entity);
		this.entity = entity;

		addSlotToContainer(new Slot(entity, 0, 53, 24));
		addSlotToContainer(new SlotBlockedInventory(entity, 1, 107, 24));

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 62 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 120));
		}
	}

	@Nonnull
    @Override
	public ItemStack transferStackInSlot(EntityPlayer player, int id) {
		return transfer.transferStackInSlot(this, entity, player, id);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return entity.isUsableByPlayer(player);
	}

}
