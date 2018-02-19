package sonar.logistics.api.displays.elements;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

public interface IClickableElement extends IDisplayElement {

	/**
	 * 
	 * @param click
	 * @param player
	 * @param subClickX
	 * @param subClickY
	 * @return the GUI ID to open
	 */
	int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY);
}
