package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2;
import sonar.logistics.common.multiparts.wireless.TileDataReceiver;
import sonar.logistics.networking.connections.WirelessDataHandler;

public class ContainerDataReceiver extends ContainerMultipartSync {

	public ContainerDataReceiver(TileDataReceiver tileDataReceiver) {
		super(tileDataReceiver);
	}
	
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			PL2.getWirelessManager().removeViewer(player);
	}
}
