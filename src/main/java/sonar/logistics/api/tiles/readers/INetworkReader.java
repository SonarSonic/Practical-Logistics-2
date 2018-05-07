package sonar.logistics.api.tiles.readers;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.IChannelledTile;
import sonar.logistics.api.tiles.nodes.NodeConnection;

import java.util.List;

/** a reader which is controlled by the network */
public interface INetworkReader<T extends IInfo> extends IChannelledTile, IInfoProvider, IListReader<T> {

	void setMonitoredInfo(AbstractChangeableList<T> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid);
}
