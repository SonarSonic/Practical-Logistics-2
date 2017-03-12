package sonar.logistics.api.readers;

import java.util.ArrayList;
import java.util.UUID;

import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;

/** a reader which is controlled by the network */
public interface INetworkReader<T extends IMonitorInfo> extends IChannelledTile, IInfoProvider, IListReader<T> {

	public void setMonitoredInfo(MonitoredList<T> updateInfo, ArrayList<NodeConnection> usedChannels, int channelID);

	/** the multipart UUID */
	public UUID getUUID();

}
