package sonar.logistics.core.tiles.readers.info;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.handlers.inventories.containers.ContainerMultipartSync;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.core.tiles.readers.base.TileAbstractLogicReader;

public class ContainerInfoReader extends ContainerMultipartSync {
	public TileAbstractLogicReader<IProvidableInfo> reader;

	public ContainerInfoReader(EntityPlayer player, TileAbstractLogicReader<IProvidableInfo> tile) {
		super(tile);
		this.reader = tile;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			reader.getListenerList().removeListener(player, true, ListenerType.OLD_GUI_LISTENER);
	}

	public boolean syncInventory() {
		return false;
	}
}
