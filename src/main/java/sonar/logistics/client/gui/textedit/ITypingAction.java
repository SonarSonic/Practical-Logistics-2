package sonar.logistics.client.gui.textedit;

public interface ITypingAction {

	public void trigger(GuiEditStyledStrings s, StyledStringLine string, char c, int i);
}