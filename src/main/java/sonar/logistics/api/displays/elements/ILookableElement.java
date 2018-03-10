package sonar.logistics.api.displays.elements;

public interface ILookableElement extends IDisplayElement {

	default boolean isPlayerLooking(){
		return this == this.getGSI().lookElement;
	}
}
