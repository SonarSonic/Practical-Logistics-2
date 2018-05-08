package sonar.logistics.core.tiles.displays.gsi.modes;

import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.buttons.ButtonEmptyInfo;
import sonar.logistics.core.tiles.displays.info.types.text.StyledTitleElement;
import sonar.logistics.core.tiles.displays.info.types.text.StyledWrappedTextElement;

import javax.annotation.Nullable;

public enum GSICreateInfo {
	INFO(GSICreateInfo::createInfo), //
	TITLE(GSICreateInfo::createTitle), //
	WRAPPED_TEXT(GSICreateInfo::createWrappedText), //
	IMAGE(GSICreateInfo::createInfo), //
	BUTTON(GSICreateInfo::createButton); //
	public ICreationLogic logic;

	GSICreateInfo(ICreationLogic logic) {
		this.logic = logic;
	}

	public interface ICreationLogic {
		@Nullable
        IDisplayElement create(DisplayElementContainer c);
	}

	public static IDisplayElement createInfo(DisplayElementContainer c) {
		ButtonEmptyInfo e = new ButtonEmptyInfo();
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
