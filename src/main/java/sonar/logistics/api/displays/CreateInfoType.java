package sonar.logistics.api.displays;

import javax.annotation.Nullable;

import sonar.logistics.api.displays.buttons.EmptyInfoElement;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.DisplayElementList;
import sonar.logistics.api.displays.elements.TextDisplayElement;

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
	
	public static IDisplayElement createInfo(DisplayElementContainer c){
		EmptyInfoElement e = new EmptyInfoElement();
		c.getElements().addElement(e);
		return e;
	}
	
	public static IDisplayElement createImage(DisplayElementContainer c){
		return null;
	}
	
	public static IDisplayElement createButton(DisplayElementContainer c){
		return null;
	}
	
	public static IDisplayElement createText(DisplayElementContainer c){
		DisplayElementList e = new DisplayElementList();
		c.getElements().addElement(e);
		e.getElements().addElement(new TextDisplayElement("CLICK TO EDIT TEXT"));
		return e;
	}
}
