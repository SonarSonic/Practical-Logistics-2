package sonar.logistics.api.networks;

import java.util.Map;

import sonar.core.utils.Pair;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.MonitoredList;

public interface INetworkListHandler<I extends IInfo> extends INetworkHandler {

	//public String id();
	
	public int updateRate();

	public Class<? extends INetworkListChannels> getChannelsType();

	public MonitoredList<I> updateConnection(INetworkListChannels channels, MonitoredList<I> newList, MonitoredList<I> oldList, NodeConnection c);
	
	public Map<NodeConnection, MonitoredList<I>> getAllChannels(Map<NodeConnection, MonitoredList<I>> compiling, Map<NodeConnection, MonitoredList<I>> oldList, ILogisticsNetwork network);

	public Pair<InfoUUID, MonitoredList<I>> updateAndSendList(ILogisticsNetwork network, IListReader<I> reader, Map<NodeConnection, MonitoredList<I>> channelLists, boolean send);

}
