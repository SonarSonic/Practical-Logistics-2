package sonar.logistics.api.networks;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import sonar.core.utils.Pair;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.utils.MonitoredList;

public interface INetworkListHandler<I extends IMonitorInfo> extends INetworkHandler {
	
	public @Nullable ChannelList getChannelsList(ILogisticsNetwork network, List<IListReader<I>> readers);

	public MonitoredList<I> updateConnection(MonitoredList<I> newList, MonitoredList<I> oldList, NodeConnection c, ChannelList channels);
	
	public Map<NodeConnection, MonitoredList<I>> getAllChannels(Map<NodeConnection, MonitoredList<I>> compiling, ILogisticsNetwork network);

	public Pair<InfoUUID, MonitoredList<I>> updateAndSendList(ILogisticsNetwork network, IListReader<I> reader, Map<NodeConnection, MonitoredList<I>> channelLists, boolean send);

}
