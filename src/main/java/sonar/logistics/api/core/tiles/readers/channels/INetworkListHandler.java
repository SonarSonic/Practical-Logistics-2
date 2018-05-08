package sonar.logistics.api.core.tiles.readers.channels;

import sonar.core.utils.Pair;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.base.channels.NodeConnection;

import java.util.Map;

public interface INetworkListHandler<I extends IInfo, L extends AbstractChangeableList> extends INetworkHandler {

	//public String id();

	int updateRate();

	Class<? extends INetworkListChannels> getChannelsType();
	
	L newChangeableList();

	L updateConnection(INetworkListChannels channels, L list, NodeConnection c);
	
	Map<NodeConnection, L> getAllChannels(Map<NodeConnection, L> list, ILogisticsNetwork network);

	Pair<InfoUUID, AbstractChangeableList<I>> updateAndSendList(ILogisticsNetwork network, IListReader<I> reader, Map<NodeConnection, AbstractChangeableList<I>> channelLists, boolean send);

}
