package sonar.logistics.core.tiles.displays.info.elements.buttons;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.modes.GSICreateInfo;

public class ButtonCreateElement extends ButtonElement {

	public GSICreateInfo type;

	public ButtonCreateElement(GSICreateInfo type, int buttonID, int buttonX, int buttonY, String hoverString) {
		super(buttonID, buttonX, buttonY, hoverString);
		this.type = type;
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {	
		click.gsi.grid_mode.startGridSelectionMode(type);
		return -1;		
	}

}
