package sonar.logistics.api.core.tiles.wireless.transceivers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;

public interface IRangeTransceiver {

	/** the connected BlockCoords, this should include dimension, could be null */
    BlockCoords getFirstCoords(ItemStack stack);

	/** the connected BlockCoords, this should include dimension, could be null */
    BlockCoords getSecondCoords(ItemStack stack);

	/** the side the transceiver was clicked on, could be null */
    EnumFacing getDirection(ItemStack stack);
	
}
