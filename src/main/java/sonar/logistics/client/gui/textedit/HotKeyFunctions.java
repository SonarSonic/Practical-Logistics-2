package sonar.logistics.client.gui.textedit;

import net.minecraft.client.gui.GuiScreen;

public enum HotKeyFunctions {

	SELECT_ALL((c, i) -> GuiScreen.isKeyComboCtrlA(i), GuiActions.SELECT_ALL), // select all
	DESELECT_ALL((c, i) -> c == 4, GuiActions.DESELECT_ALL); // deselect all

	public IKeyMatch key;
	public IGuiAction action;

	HotKeyFunctions(IKeyMatch key, IGuiAction action) {
		this.key = key;
		this.action = action;

	}

	public static boolean checkFunction(GuiEditStyledStrings gui, char c, int i) {
		for (HotKeyFunctions func : HotKeyFunctions.values()) {
			if (func.key.canTriggerFunction(c, i)) {
				func.action.trigger(gui);
				return true;
			}
		}
		return false;
	}
}
