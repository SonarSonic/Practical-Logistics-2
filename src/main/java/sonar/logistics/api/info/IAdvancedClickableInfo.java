package sonar.logistics.api.info;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

/** implemented on info which can be clicked by the player */
public interface IAdvancedClickableInfo{

	public boolean canClick(DisplayScreenClick click, DisplayInfo renderInfo, EntityPlayer player, EnumHand hand);
	
	/** @param event the screen interaction event
	 * @param renderInfo the infos current render properties
	 * @param player the player who clicked the info
	 * @param hand players hand
	 * @param stack players held item
	 * @param container the displays container
	 * @return if the screen was clicked */
	public NBTTagCompound createClickPacket(DisplayScreenClick click, DisplayInfo renderInfo, EntityPlayer player, EnumHand hand);
		
	/**called on server side after the screen has been clicked client side
	 * @param click TODO
	 * @param player TODO*/
	public void runClickPacket(DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag);
		
}
