package sonar.logistics.api.tiles;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.viewers.ILogicListenable;

/**a tile which has channels which can be configured by the operator*/
public interface IChannelledTile extends ILogicListenable {

	/**the currently selected channels*/
    ChannelList getChannels();
	
	/**this tiles channel type*/
    ChannelType channelType();
	
	/**call this client side only, sends the selected coords to the server
	 * @param channelID the id to modify the coords on*/
    void sendCoordsToServer(IInfo coords, int channelID);
}
