package sonar.logistics.core.tiles.displays.info.types.text.gui.hotkeys;

import sonar.logistics.core.tiles.displays.info.types.text.gui.GuiStyledStringFunctions;

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
