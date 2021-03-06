package sonar.logistics.core.items.wirelessstoragereader;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import sonar.logistics.core.tiles.wireless.handling.WirelessDataManager;

import javax.annotation.Nonnull;

public class ContainerEmitterList extends Container {
	public EntityPlayer player;

	public ContainerEmitterList(EntityPlayer player) {
		this.player = player;
	}
	
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			WirelessDataManager.instance().removeViewer(player);
	}

	@Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer player) {
		return true;
	}

}