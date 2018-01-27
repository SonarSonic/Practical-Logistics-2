package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.common.multiparts.wireless.TileAbstractReceiver;

public class ContainerDataReceiver extends ContainerMultipartSync {
	public TileAbstractReceiver receiver;

	public ContainerDataReceiver(TileAbstractReceiver receiver) {
		super(receiver);
		this.receiver = receiver;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			receiver.getWirelessHandler().removeViewer(player);
	}
}
