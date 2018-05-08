package sonar.logistics.api.core.tiles.wireless.transceivers;

import net.minecraft.item.ItemStack;

/** implemented on Items which can provide Channels for a handling when placed in a Array */
public interface ITransceiver {

	/** gets the BlockStack retrieved from the last clicked coordinates, typically used for displaying the name, could be null */
    String getUnlocalizedName(ItemStack stack);
}
