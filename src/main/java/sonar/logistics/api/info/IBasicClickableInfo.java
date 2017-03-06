package sonar.logistics.api.info;

import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;

/** implemented on info which can be clicked by the player */
public interface IBasicClickableInfo{

	/** @param type the type of click
	 * @param doubleClick if the player double clicked
	 * @param renderInfo the infos current render properties
	 * @param player the player who clicked the info
	 * @param hand players hand
	 * @param stack players held item
	 * @param hit the RayTrace hit info
	 * @param container the displays info container
	 * @return if the screen was clicked */
	public boolean onStandardClick(BlockInteractionType type, boolean doubleClick, IDisplayInfo renderInfo, EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit, InfoContainer container);
				
}
