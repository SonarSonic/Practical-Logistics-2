package sonar.logistics.core.tiles.displays.info.elements.buttons;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.modes.GSIElementSelection;

public class ButtonElementSelection extends ButtonElement {

	public GSIElementSelection type;

	public ButtonElementSelection(GSIElementSelection type, int buttonID, int buttonX, int buttonY, String hoverString) {
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
