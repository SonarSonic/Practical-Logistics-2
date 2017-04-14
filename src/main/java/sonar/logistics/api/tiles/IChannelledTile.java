package sonar.logistics.api.tiles;

import sonar.core.listener.PlayerListener;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.viewers.ILogicListenable;

/**a tile which has channels which can be configured by the operator*/
public interface IChannelledTile extends ILogicListenable<PlayerListener> {

	/**the currently selected channels*/
	public ChannelList getChannels();
	
	/**this tiles channel type*/
	public ChannelType channelType();
	
	/**call this client side only, sends the selected coords to the server
	 * @param channelID the id to modify the coords on*/
	public void sendCoordsToServer(IInfo coords, int channelID);
}
