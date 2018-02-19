package sonar.logistics.api.displays.elements;

import sonar.logistics.api.displays.IDisplayElement;

public interface ILookableElement extends IDisplayElement {

	default boolean isPlayerLooking(){
		return this == this.getGSI().lookElement;
	}
}
