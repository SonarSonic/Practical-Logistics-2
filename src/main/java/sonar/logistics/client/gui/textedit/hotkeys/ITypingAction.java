package sonar.logistics.client.gui.textedit.hotkeys;

import sonar.logistics.api.displays.elements.text.StyledStringLine;
import sonar.logistics.client.gui.textedit.GuiEditStyledStrings;

public interface ITypingAction {

	public void trigger(GuiEditStyledStrings s, StyledStringLine line, char c, int i);
}