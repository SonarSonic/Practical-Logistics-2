package sonar.logistics.common.multiparts.wireless;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import sonar.core.network.sync.SyncUUID;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.common.multiparts.TileSidedLogistics;

import java.util.UUID;

public class TileAbstractWireless extends TileSidedLogistics {
	
	public SyncUUID playerUUID = new SyncUUID(3);

	{syncList.addParts(playerUUID);}

	public UUID getOwner() {
		return playerUUID.getUUID();
	}

	public boolean isOwner(EntityPlayer player) {
		return player != null && playerUUID.getUUID().equals(player.getGameProfile().getId());
	}

	public TileSidedLogistics setOwner(EntityPlayer player) {
		if (player != null) {
			playerUUID.setObject(player.getGameProfile().getId());
		}
		return this;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.HALF;
	}

}
