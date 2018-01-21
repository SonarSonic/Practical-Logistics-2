package sonar.logistics.networking.handlers;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.networking.channels.NetworkWatcherChannels;

public class NetworkWatcherHandler implements INetworkHandler {

	public static NetworkWatcherHandler INSTANCE = new NetworkWatcherHandler();
	
	@Override
	public int updateRate() {
		return 20;
	}

	@Override
	public Class<? extends INetworkChannels> getChannelsType() {
		return NetworkWatcherChannels.class;
	}

	private InfoChangeableList newChangeableList() {
		return new InfoChangeableList();
	}

	public AbstractChangeableList<IProvidableInfo> updateNetworkList(AbstractChangeableList<IProvidableInfo> list, ILogisticsNetwork network){
		List<IProvidableInfo> providedInfo = Lists.newArrayList();
		LogicInfoRegistry.INSTANCE.getTileInfo(providedInfo, null, network);
		providedInfo.forEach(info -> list.add(info));	
		return list;		
	}

	public Pair<InfoUUID, AbstractChangeableList<IProvidableInfo>> updateAndSendList(ILogisticsNetwork network, IListReader<IProvidableInfo> reader, AbstractChangeableList<IProvidableInfo> networkList, boolean send) {
		InfoUUID uuid = getReaderUUID(reader);
		if (network.validateTile(reader)) {
			List<NodeConnection> usedChannels = Lists.newArrayList();
			AbstractChangeableList<IProvidableInfo> updateList = (updateList = PL2.getServerManager().getMonitoredList(uuid)) == null ? newChangeableList() : updateList;
			updateList.saveStates();
			AbstractChangeableList<IProvidableInfo> viewableList = reader.getViewableList(updateList, uuid, Maps.newHashMap(), usedChannels);
			if (reader instanceof INetworkReader) {
				((INetworkReader) reader).setMonitoredInfo(updateList, usedChannels, uuid);
			}
			PL2.getServerManager().monitoredLists.put(uuid, updateList);
			if (send && (!updateList.wasLastListNull || updateList.wasLastListNull != updateList.getList().isEmpty()))
				PacketHelper.sendReaderToListeners(reader, updateList, uuid);
			return new Pair(uuid, updateList);
		}
		return new Pair(uuid, newChangeableList());
	}
}
