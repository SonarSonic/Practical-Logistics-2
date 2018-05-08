package sonar.logistics.core.tiles.displays.info.elements.base;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;

/**when implemented it allows the element to be clicked by a player when it is displayed on a screen*/
public interface IClickableElement extends IDisplayElement {

	/** called when the player clicks the Display Screen on this specific element
	 * @param click - the particulars of the click
	 * @param player - the player clicking the element
	 * @param subClickX - the x click position within this element, taking out alignment and translation
	 * @param subClickY - the y click position within this element, taking out alignment and translation
	 * @return the GUI ID to open, this is a GUI ID of this paticular clickable element */
	int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY);
}
