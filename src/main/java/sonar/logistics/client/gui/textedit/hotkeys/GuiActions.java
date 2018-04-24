package sonar.logistics.client.gui.textedit.hotkeys;

import sonar.logistics.client.gui.textedit.GuiStyledStringFunctions;

public enum GuiActions implements IGuiAction {

	SELECT_ALL(GuiStyledStringFunctions::selectAll),
	DESELECT_ALL(GuiStyledStringFunctions::deselectAll),
	DELETE_SELECTED(GuiStyledStringFunctions::deleteAllSelected),
	UPDATE_TEXT_SCALING(t -> t.text.onElementChanged());

	public IGuiAction action;

	GuiActions(IGuiAction action) {
		this.action = action;
	}

	@Override
	public void trigger(GuiStyledStringFunctions trigger) {
		action.trigger(trigger);
	}

}
