package sonar.logistics.api.tiles.displays;

import sonar.logistics.api.cabling.ICable;

public interface ILargeDisplay extends IDisplay, ICable {

	public ConnectedDisplay getConnectedDisplay();

	public void setConnectedDisplay(ConnectedDisplay connectedDisplay);

	/** if this {@link ILargeDisplay} should render the info from the {@link ConnectedDisplay} */
	public boolean shouldRender();

	/** sets if this {@link ILargeDisplay} should be responsible for rendering the data from the {@link ConnectedDisplay} */
	public void setShouldRender(boolean shouldRender);
	
	public void setLocked(boolean locked); 
}
