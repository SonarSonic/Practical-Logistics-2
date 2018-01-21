package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.readers.TileAbstractLogicReader;
import sonar.logistics.common.multiparts.readers.TileInfoReader;

public class ContainerInfoReader extends ContainerMultipartSync {
	public TileAbstractLogicReader<IProvidableInfo> reader;

	public ContainerInfoReader(EntityPlayer player, TileAbstractLogicReader<IProvidableInfo> tile) {
		super(tile);
		this.reader = tile;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			reader.getListenerList().removeListener(player, true, ListenerType.LISTENER);
	}

	public boolean syncInventory() {
		return false;
	}
}
