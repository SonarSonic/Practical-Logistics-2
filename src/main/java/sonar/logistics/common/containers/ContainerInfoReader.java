package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.readers.InfoReaderPart;

public class ContainerInfoReader extends ContainerMultipartSync {
	public InfoReaderPart reader;

	public ContainerInfoReader(EntityPlayer player, InfoReaderPart reader) {
		super(reader);
		this.reader = reader;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			reader.getListenerList().removeListener(player, true, ListenerType.INFO);
	}

	public boolean syncInventory() {
		return false;
	}
}
