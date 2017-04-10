package sonar.logistics.common.multiparts.generic;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.network.sync.SyncUUID;

public abstract class WirelessPart extends SidedMultipart {
	public SyncUUID playerUUID = new SyncUUID(3);
	
	{syncList.addParts(playerUUID);}

	public SidedMultipart setOwner(EntityPlayer player) {
		if (player != null) {
			playerUUID.setObject(player.getGameProfile().getId());
		}
		return this;
	}

}
