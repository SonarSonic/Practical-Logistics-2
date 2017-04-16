package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.InventoryHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.tiles.IChannelledTile;

public class ContainerChannelSelection extends ContainerMultipartSync {

	public IChannelledTile tile;

	public ContainerChannelSelection(IChannelledTile tile) {
		super((SonarMultipart) tile);
		this.tile = tile;
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return InventoryHelper.EMPTY;
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