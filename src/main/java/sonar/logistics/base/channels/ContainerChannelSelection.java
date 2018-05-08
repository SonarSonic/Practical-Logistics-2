package sonar.logistics.base.channels;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.base.tiles.IChannelledTile;

public class ContainerChannelSelection extends ContainerMultipartSync {

	public IChannelledTile tile;

	public ContainerChannelSelection(IChannelledTile tile) {
		super((TileSonarMultipart) tile);
		this.tile = tile;
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return ItemStack.EMPTY;
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