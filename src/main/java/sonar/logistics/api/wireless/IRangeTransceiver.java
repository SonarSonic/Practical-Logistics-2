package sonar.logistics.api.wireless;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;

public interface IRangeTransceiver {

	/** the connected BlockCoords, this should include dimension, could be null */
	public BlockCoords getFirstCoords(ItemStack stack);

	/** the connected BlockCoords, this should include dimension, could be null */
	public BlockCoords getSecondCoords(ItemStack stack);

	/** the side the transceiver was clicked on, could be null */
	public EnumFacing getDirection(ItemStack stack);
	
}
