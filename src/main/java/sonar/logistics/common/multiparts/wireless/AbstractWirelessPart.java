package sonar.logistics.common.multiparts.wireless;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.network.sync.SyncUUID;
import sonar.logistics.common.multiparts.SidedPart;

public abstract class AbstractWirelessPart extends SidedPart {
	public SyncUUID playerUUID = new SyncUUID(3);
	
	{syncList.addParts(playerUUID);}

	public SidedPart setOwner(EntityPlayer player) {
		if (player != null) {
			playerUUID.setObject(player.getGameProfile().getId());
		}
		return this;
	}

}
