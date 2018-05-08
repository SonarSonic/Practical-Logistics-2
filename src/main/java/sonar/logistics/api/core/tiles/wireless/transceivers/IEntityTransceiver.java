package sonar.logistics.api.core.tiles.wireless.transceivers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.UUID;

/** implemented on Items which can provide Channels for a handling when placed in a Array */
public interface IEntityTransceiver extends ITransceiver {

	ItemStack onRightClickEntity(EntityPlayer player, ItemStack stack, Entity entity, EnumHand hand);
	
	/** the UUID of the clicked entity, this should include dimension, could be null */
    UUID getEntityUUID(ItemStack stack);
}
