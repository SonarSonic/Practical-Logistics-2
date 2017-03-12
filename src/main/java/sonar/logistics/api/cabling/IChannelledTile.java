package sonar.logistics.api.cabling;

import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.readers.IdentifiedChannelsList;
import sonar.logistics.api.viewers.ILogicViewable;

/**a tile which has channels which can be configured by the operator*/
public interface IChannelledTile extends ILogicViewable {

	/**the currently selected channels*/
	public IdentifiedChannelsList getChannels();
	
	/**call this client side only, sends the selected coords to the server
	 * @param channelID the id to modify the coords on*/
	public void modifyCoords(IMonitorInfo coords, int channelID);
	
	public ChannelType channelType();
}
