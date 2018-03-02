package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.common.multiparts.wireless.DataReceiverPart;
import sonar.logistics.managers.WirelessManager;

public class ContainerDataReceiver extends ContainerMultipartSync {

	public ContainerDataReceiver(DataReceiverPart entity) {
		super(entity);
	}
	
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.getEntityWorld().isRemote)
			WirelessManager.removeViewer(player);
	}
}
