package sonar.logistics.api.core.tiles.wireless.transceivers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;

/** implemented on Items which can provide Channels for a handling when placed in a Array */
public interface ITileTransceiver extends ITransceiver {

	/** the connected BlockCoords, this should include dimension, could be null */
    BlockCoords getCoords(ItemStack stack);

	/** the side the transceiver was clicked on, could be null */
    EnumFacing getDirection(ItemStack stack);
	
	
}
