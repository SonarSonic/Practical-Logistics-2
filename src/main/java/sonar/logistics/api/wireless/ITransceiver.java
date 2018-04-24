package sonar.logistics.api.wireless;

import net.minecraft.item.ItemStack;

/** implemented on Items which can provide Channels for a network when placed in a Array */
public interface ITransceiver {

	/** gets the BlockStack retrieved from the last clicked coordinates, typically used for displaying the name, could be null */
    String getUnlocalizedName(ItemStack stack);
}
