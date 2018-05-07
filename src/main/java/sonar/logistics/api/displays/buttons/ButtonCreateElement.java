package sonar.logistics.api.displays.buttons;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.displays.CreateInfoType;
import sonar.logistics.api.displays.tiles.DisplayScreenClick;

public class ButtonCreateElement extends ButtonElement {

	public CreateInfoType type;

	public ButtonCreateElement(CreateInfoType type, int buttonID, int buttonX, int buttonY, String hoverString) {
		super(buttonID, buttonX, buttonY, hoverString);
		this.type = type;
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {	
		click.gsi.grid_mode.startGridSelectionMode(type);
		return -1;		
	}

}
