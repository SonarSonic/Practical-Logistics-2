package sonar.logistics.api.core.tiles.readers;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.tiles.IChannelledTile;

import java.util.List;

/** a reader which is controlled by the handling */
public interface INetworkReader<T extends IInfo> extends IChannelledTile, IInfoProvider, IListReader<T> {

	void setMonitoredInfo(AbstractChangeableList<T> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid);
}
