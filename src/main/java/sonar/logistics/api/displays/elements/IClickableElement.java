package sonar.logistics.api.displays.elements;

import net.minecraft.util.EnumHand;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

public interface IClickableElement extends IDisplayElement {

	boolean onGSIClicked(DisplayScreenClick click, double subClickX, double subClickY);
}
