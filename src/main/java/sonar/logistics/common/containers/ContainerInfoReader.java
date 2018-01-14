package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.readers.TileInfoReader;

public class ContainerInfoReader extends ContainerMultipartSync {
	public TileInfoReader reader;

	public ContainerInfoReader(EntityPlayer player, TileInfoReader reader) {
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
