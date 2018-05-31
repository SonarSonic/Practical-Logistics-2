package sonar.logistics.core.tiles.wireless.receivers;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.inventory.containers.ContainerMultipartSync;

public class ContainerAbstractReceiver extends ContainerMultipartSync {
	public TileAbstractReceiver receiver;

	public ContainerAbstractReceiver(TileAbstractReceiver receiver) {
		super(receiver);
		this.receiver = receiver;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			receiver.getWirelessHandler().removeViewer(player);
	}
}
