package sonar.logistics.base.tiles;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.base.channels.ChannelList;
import sonar.logistics.base.channels.ChannelType;
import sonar.logistics.base.listeners.ILogicListenable;

/**a tile which has channels which can be configured by the operator*/
public interface IChannelledTile extends ILogicListenable {

	/**the currently selected channels*/
    ChannelList getChannels();
	
	/**this base channel type*/
    ChannelType channelType();
	
	/**call this client side only, sends the selected coords to the server
	 * @param channelID the id to modify the coords on*/
    void sendCoordsToServer(IInfo coords, int channelID);
}
