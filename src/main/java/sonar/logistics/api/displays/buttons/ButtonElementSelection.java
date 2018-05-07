package sonar.logistics.api.displays.buttons;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.displays.elements.ElementSelectionType;
import sonar.logistics.api.displays.tiles.DisplayScreenClick;

public class ButtonElementSelection extends ButtonElement {

	public ElementSelectionType type;

	public ButtonElementSelection(ElementSelectionType type, int buttonID, int buttonX, int buttonY, String hoverString) {
		super(buttonID, buttonX, buttonY, hoverString);
		this.type = type;
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		//if (click.gsi.isElementSelectionMode && click.gsi.selectionType == type && type.shouldCollect()) {
			//click.gsi.finishElementSelectionMode();
		//} else {
			click.gsi.selection_mode.startElementSelectionMode(type);
		//}
		return -1;
	}

}
