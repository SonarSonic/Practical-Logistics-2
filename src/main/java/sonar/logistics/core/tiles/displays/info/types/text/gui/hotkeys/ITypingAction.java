package sonar.logistics.core.tiles.displays.info.types.text.gui.hotkeys;

import sonar.logistics.core.tiles.displays.info.types.text.gui.GuiEditStyledStrings;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledStringLine;

public interface ITypingAction {

	void trigger(GuiEditStyledStrings s, StyledStringLine line, char c, int i);
}