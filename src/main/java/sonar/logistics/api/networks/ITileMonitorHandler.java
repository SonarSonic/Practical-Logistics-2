package sonar.logistics.api.networks;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.utils.MonitoredList;

/** implemented on {@link INetworkHandler}s which can provide info for Tiles */
public interface ITileMonitorHandler<I extends IInfo, C extends INetworkChannels> extends INetworkHandler {

	/** updates info for the given tile.
	 * @param channels the {@link INetworkChannels} calling for the updated info
	 * @param newList the new list given by the {@link INetworkChannels}
	 * @param previousList the last list the handler gave for this connection
	 * @param connection the tile connection to provide info for
	 * @return the list to set the updated info to. */
	public MonitoredList<I> updateInfo(C channels, MonitoredList<I> newList, MonitoredList<I> previousList, BlockConnection connection);
}
