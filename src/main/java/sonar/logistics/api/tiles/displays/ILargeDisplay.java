package sonar.logistics.api.tiles.displays;

import sonar.logistics.api.cabling.ICable;

public interface ILargeDisplay extends IDisplay, ICable {

	ConnectedDisplay getConnectedDisplay();

	void setConnectedDisplay(ConnectedDisplay connectedDisplay);

	/** if this {@link ILargeDisplay} should render the info from the {@link ConnectedDisplay} */
    boolean shouldRender();

	/** sets if this {@link ILargeDisplay} should be responsible for rendering the data from the {@link ConnectedDisplay} */
    void setShouldRender(boolean shouldRender);
	
	void setLocked(boolean locked);
}
