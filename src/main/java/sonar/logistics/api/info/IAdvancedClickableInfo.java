package sonar.logistics.api.info;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayInteractionEvent;

/** implemented on info which can be clicked by the player */
public interface IAdvancedClickableInfo{

	/** @param event the screen interaction event
	 * @param renderInfo the infos current render properties
	 * @param player the player who clicked the info
	 * @param hand players hand
	 * @param stack players held item
	 * @param container the displays container
	 * @return if the screen was clicked */
	public NBTTagCompound onClientClick(DisplayInteractionEvent event, IDisplayInfo renderInfo, EntityPlayer player, ItemStack stack, InfoContainer container);
		
	/**called on server side after the screen has been clicked client side*/
	public void onClickEvent(InfoContainer container, IDisplayInfo displayInfo, DisplayInteractionEvent event, NBTTagCompound tag);
		
}
