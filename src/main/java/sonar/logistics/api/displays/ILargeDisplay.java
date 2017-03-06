package sonar.logistics.api.displays;

import sonar.logistics.api.cabling.INetworkConnectable;

/**implemented on Large Display Screen*/
public interface ILargeDisplay extends IInfoDisplay,INetworkConnectable {
	
	/**gets the {@link ConnectedDisplayScreen} this {@link ILargeDisplay} is connected to*/
	public ConnectedDisplayScreen getDisplayScreen();
	
	/**sets the {@link ConnectedDisplayScreen} this {@link ILargeDisplay} is connected to*/
	public void setConnectedDisplay(ConnectedDisplayScreen connectedDisplay);
	
	/**if this {@link ILargeDisplay} should render the info from the {@link ConnectedDisplayScreen}*/
	public boolean shouldRender();
	
	/**sets if this {@link ILargeDisplay} should be responsible for rendering the data from the {@link ConnectedDisplayScreen}*/
	public void setShouldRender(boolean shouldRender);
}
