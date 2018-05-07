package sonar.logistics.api.displays.elements;

import sonar.logistics.api.displays.tiles.DisplayScreenLook;
import sonar.logistics.client.gsi.GSIOverlays;

/**Implementing this on an IDisplayElement doesn't require any additional methods,
 * however elements which override this will be checked to see if the client is looking at them.
 * You should use the default method {@link #isPlayerLooking()} to see if this is true or not at the time of rendering.
 * 
 * Then use the {@link #getCurrentLook()} to get the details of the {@link DisplayScreenLook}.*/
public interface ILookableElement extends IDisplayElement {

	/**returns if the player is looking at this particular element*/
	default boolean isPlayerLooking(){
		return this == this.getGSI().lookElement;
	}
	
	/**obtains the current {@link DisplayScreenLook} from the client*/
	default DisplayScreenLook getCurrentLook(){
		return GSIOverlays.getCurrentLook(this.getGSI());
	}
}
