package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.logistics.ILogisticsTile;

public class ContainerStatementList extends ContainerMultipartSync {

	public ILogisticsTile tile;

	public ContainerStatementList(EntityPlayer player, ILogisticsTile tile) {
		super((SonarMultipart) tile);
		this.tile = tile;
		/*
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 41 + j * 18, 174 + i * 18));
			}
		}


		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(player.inventory, i, 41 + i * 18, 232));
		}
		*/
	}
		
	public boolean syncInventory() {
		return true;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return null;
	}

	public SyncType[] getSyncTypes() {
		return new SyncType[] { SyncType.DEFAULT_SYNC };
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
	}
}