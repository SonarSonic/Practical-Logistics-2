package sonar.logistics.api.displays.buttons;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.displays.CreateInfoType;
import sonar.logistics.api.displays.elements.ButtonElement;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

public class CreateElementButton extends ButtonElement {

	public CreateInfoType type;

	public CreateElementButton(CreateInfoType type, int buttonID, int buttonX, int buttonY, String hoverString) {
		super(buttonID, buttonX, buttonY, hoverString);
		this.type = type;
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {	
		click.gsi.startGridSelectionMode(type);
		return -1;		
	}

}
