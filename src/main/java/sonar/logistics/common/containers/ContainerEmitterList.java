package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import sonar.core.utils.SonarCompat;

public class ContainerEmitterList extends Container {
	public EntityPlayer player;

	public ContainerEmitterList(EntityPlayer player) {
		this.player = player;
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return SonarCompat.getEmpty();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}