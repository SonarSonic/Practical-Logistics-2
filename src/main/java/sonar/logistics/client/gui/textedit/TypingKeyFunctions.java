package sonar.logistics.client.gui.textedit;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;

public enum TypingKeyFunctions {

	/*
Ctrl – C
Ctrl – V
Shift – End/Home
Shift – Arrow Keys
Ctrl –X
Ctrl B,
Ctril I
Ctril U
Ctrl S

	 */
	UP((c, i) -> i==Keyboard.KEY_UP, (gui, string, chr, key) -> gui.cursorPosition.moveY(gui, -1)),//
	DOWN((c, i) -> i==Keyboard.KEY_DOWN, (gui, string, chr, key) -> gui.cursorPosition.moveY(gui, -1)),//
	LEFT((c, i) -> i==Keyboard.KEY_LEFT, (gui, string, chr, key) -> gui.cursorPosition.moveX(gui, -1)),//
	RIGHT((c, i) -> i==Keyboard.KEY_RIGHT, (gui, string, chr, key) -> gui.cursorPosition.moveX(gui, 1)),//
	HOME((c, i) -> i==Keyboard.KEY_HOME, (gui, string, chr, key) -> {}),//
	END((c, i) -> i==Keyboard.KEY_END, (gui, string, chr, key) -> string.getCachedUnformattedString().length()),//
	COPY((c, i) -> GuiScreen.isKeyComboCtrlC(i), (gui, string, chr, key) -> {}),//FIXME
	PASTE((c, i) -> GuiScreen.isKeyComboCtrlV(i), (gui, string, chr, key) -> {}),//FIXME
	CUT((c, i) -> GuiScreen.isKeyComboCtrlX(i), (gui, string, chr, key) -> {}),//FIXME
	BACKSPACE((c, i) -> i==Keyboard.KEY_BACK, (gui, string, chr, key) -> string.backspaceText(gui.cursorPosition.getTypingIndex(gui), 1)),//
	DEL((c, i) -> i==Keyboard.KEY_DELETE, (gui, string, chr, key) -> string.deleteText(gui.cursorPosition.getTypingIndex(gui), 1));//

	public IKeyMatch key;
	public ITypingAction action;

	TypingKeyFunctions(IKeyMatch key, ITypingAction action) {
		this.key = key;
		this.action = action;
	}

	public static boolean checkFunction(GuiEditStyledStrings gui, StyledStringLine string, char c, int i) {
		for (TypingKeyFunctions func : TypingKeyFunctions.values()) {
			if (func.key.canTriggerFunction(c, i)) {
				func.action.trigger(gui, string, c, i);
				return true;
			}
		}
		return false;
	}
}
