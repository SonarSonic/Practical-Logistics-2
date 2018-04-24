package sonar.logistics.api.wireless;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/** implemented on Items which can provide Channels for a network when placed in a Array */
public interface IEntityTransceiver extends ITransceiver {

	ItemStack onRightClickEntity(EntityPlayer player, ItemStack stack, Entity entity, EnumHand hand);
	
	/** the UUID of the clicked entity, this should include dimension, could be null */
    UUID getEntityUUID(ItemStack stack);
}
