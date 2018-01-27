package sonar.logistics.api.tiles.displays;

import sonar.logistics.api.cabling.ICable;

/** implemented on Large Display Screen */
public interface ILargeDisplay extends IDisplay, ICable {

	/** gets the {@link ConnectedDisplay} this {@link ILargeDisplay} is connected to */
	public ConnectedDisplay getDisplayScreen();

	/** sets the {@link ConnectedDisplay} this {@link ILargeDisplay} is connected to */
	public void setConnectedDisplay(ConnectedDisplay connectedDisplay);

	/** if this {@link ILargeDisplay} should render the info from the {@link ConnectedDisplay} */
	public boolean shouldRender();

	/** sets if this {@link ILargeDisplay} should be responsible for rendering the data from the {@link ConnectedDisplay} */
	public void setShouldRender(boolean shouldRender);
	
	public void setLocked(boolean locked); 
}
