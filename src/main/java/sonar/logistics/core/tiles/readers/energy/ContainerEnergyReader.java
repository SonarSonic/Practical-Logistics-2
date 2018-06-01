package sonar.logistics.core.tiles.readers.energy;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.handlers.inventories.containers.ContainerMultipartSync;
import sonar.logistics.base.listeners.ListenerType;

public class ContainerEnergyReader extends ContainerMultipartSync {
	public TileEnergyReader part;

	public ContainerEnergyReader(EntityPlayer player, TileEnergyReader tileEnergyReader) {
		super(tileEnergyReader);
		this.part = tileEnergyReader;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			part.getListenerList().removeListener(player, true, ListenerType.OLD_GUI_LISTENER);
	}

	public boolean syncInventory() {
		return false;
	}
}
