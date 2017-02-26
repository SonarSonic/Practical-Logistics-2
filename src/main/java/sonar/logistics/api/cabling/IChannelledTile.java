package sonar.logistics.api.cabling;

import sonar.logistics.api.readers.IdentifiedCoordsList;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;

/**a tile which has channels which can be configured by the operator*/
public interface IChannelledTile extends ILogicViewable {

	/**the currently selected channels*/
	public IdentifiedCoordsList getChannels();
	
	/**call this client side only, sends the selected coords to the server
	 * @param channelID the id to modify the coords on*/
	public void modifyCoords(MonitoredBlockCoords coords, int channelID);
}
