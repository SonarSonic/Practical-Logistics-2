package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.readers.TileEnergyReader;

public class ContainerEnergyReader extends ContainerMultipartSync {
	public TileEnergyReader part;

	public ContainerEnergyReader(EntityPlayer player, TileEnergyReader tileEnergyReader) {
		super(tileEnergyReader);
		this.part = tileEnergyReader;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			part.getListenerList().removeListener(player, true, ListenerType.LISTENER);
	}

	public boolean syncInventory() {
		return false;
	}
}
