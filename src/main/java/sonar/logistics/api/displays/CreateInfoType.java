package sonar.logistics.api.displays;

import javax.annotation.Nullable;

import sonar.logistics.api.displays.buttons.EmptyInfoElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.text.StyledTextElement;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.displays.storage.DisplayElementList;

public enum CreateInfoType {
	INFO(CreateInfoType::createInfo), TEXT(CreateInfoType::createText), IMAGE(CreateInfoType::createInfo), BUTTON(CreateInfoType::createButton);
	public ICreationLogic logic;

	CreateInfoType(ICreationLogic logic) {
		this.logic = logic;
	}

	public interface ICreationLogic {
		@Nullable
		public IDisplayElement create(DisplayElementContainer c);
	}

	public static IDisplayElement createInfo(DisplayElementContainer c) {
		EmptyInfoElement e = new EmptyInfoElement();
		c.getElements().addElement(e);
		return e;
	}

	public static IDisplayElement createImage(DisplayElementContainer c) {
		return null;
	}

	public static IDisplayElement createButton(DisplayElementContainer c) {
		return null;
	}

	public static IDisplayElement createText(DisplayElementContainer c) {
		StyledTextElement e = new StyledTextElement("CLICK TO EDIT TEXT");
		c.getElements().addElement(e);
		return e;
	}
}
