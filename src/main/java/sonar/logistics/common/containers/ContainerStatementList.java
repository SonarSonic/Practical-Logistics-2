package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.tiles.signaller.ILogisticsTile;

public class ContainerStatementList extends ContainerMultipartSync {

	public ILogisticsTile tile;

	public ContainerStatementList(EntityPlayer player, ILogisticsTile tile) {
		super((SonarMultipart) tile);
		this.tile = tile;
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return null;
	}

	public SyncType[] getSyncTypes() {
		return new SyncType[] { SyncType.DEFAULT_SYNC };
	}

	public boolean syncInventory() {
		return true;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
}