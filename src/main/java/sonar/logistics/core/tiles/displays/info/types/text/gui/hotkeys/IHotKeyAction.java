package sonar.logistics.core.tiles.displays.info.types.text.gui.hotkeys;

import sonar.logistics.core.tiles.displays.info.types.text.gui.GuiEditStyledStrings;

public interface IHotKeyAction {

	void trigger(GuiEditStyledStrings s, char c, int i);
}