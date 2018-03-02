package sonar.logistics.client.gui.textedit;

public enum TextSelectionType {

	SET_SELECTION, SET_LINE, COMBINE, SELECT_ALL, DESELECT_ALL;

	public boolean requiresPosition() {
		return this == COMBINE || this == SET_SELECTION;
	}

}