package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.InventoryHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.readers.EnergyReaderPart;

public class ContainerEnergyReader extends ContainerMultipartSync {
	public EnergyReaderPart part;

	public ContainerEnergyReader(EntityPlayer player, EnergyReaderPart part) {
		super(part);
		this.part = part;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			part.getListenerList().removeListener(player, true, ListenerType.INFO);
	}

	public boolean syncInventory() {
		return false;
	}
}
