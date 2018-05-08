package sonar.logistics.api.core.tiles.readers.channels;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.base.channels.BlockConnection;

/** implemented on {@link INetworkHandler}s which can provide info for Tiles */
public interface ITileMonitorHandler<I extends IInfo, L extends AbstractChangeableList, C extends INetworkChannels> extends INetworkHandler {

	/** updates info for the given tile.
	 * @param channels the {@link INetworkChannels} calling for the updated info
	 * @param newList the new list given by the {@link INetworkChannels}
	 * @param connection the tile connection to provide info for
	 * @return the list to set the updated info to. */
    L updateInfo(C channels, L newList, BlockConnection connection);
}
