package sonar.logistics.api.displays;

import javax.annotation.Nullable;

import sonar.logistics.api.displays.buttons.EmptyInfoElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.text.StyledTitleElement;
import sonar.logistics.api.displays.elements.text.StyledWrappedTextElement;
import sonar.logistics.api.displays.storage.DisplayElementContainer;

public enum CreateInfoType {
	INFO(CreateInfoType::createInfo), TITLE(CreateInfoType::createTitle), WRAPPED_TEXT(CreateInfoType::createWrappedText), IMAGE(CreateInfoType::createInfo), BUTTON(CreateInfoType::createButton);
	public ICreationLogic logic;

	CreateInfoType(ICreationLogic logic) {
		this.logic = logic;
	}

	public interface ICreationLogic {
		@Nullable
        IDisplayElement create(DisplayElementContainer c);
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

	public static IDisplayElement createTitle(DisplayElementContainer c) {
		StyledTitleElement e = new StyledTitleElement("CLICK TO EDIT TEXT");
		c.getElements().addElement(e);
		return e;
	}

	public static IDisplayElement createWrappedText(DisplayElementContainer c) {
		StyledWrappedTextElement e = new StyledWrappedTextElement("CLICK TO EDIT TEXT");
		c.getElements().addElement(e);
		return e;
	}
}
