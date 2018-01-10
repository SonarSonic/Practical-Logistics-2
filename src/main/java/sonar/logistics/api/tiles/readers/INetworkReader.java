package sonar.logistics.api.tiles.readers;

import java.util.List;
import java.util.UUID;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.IChannelledTile;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.utils.MonitoredList;

/** a reader which is controlled by the network */
public interface INetworkReader<T extends IInfo> extends IChannelledTile, IInfoProvider, IListReader<T> {

	public void setMonitoredInfo(MonitoredList<T> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid);

}
