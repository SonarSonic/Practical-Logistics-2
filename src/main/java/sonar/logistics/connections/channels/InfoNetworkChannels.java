package sonar.logistics.connections.channels;

import javax.annotation.Nullable;

import sonar.logistics.PL2;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.connections.handlers.InfoNetworkHandler;

public class InfoNetworkChannels extends ListNetworkChannels<IProvidableInfo, InfoNetworkHandler> {

	private ChannelList currentList;

	public InfoNetworkChannels(ILogisticsNetwork network) {
		super(InfoNetworkHandler.INSTANCE, network);
	}

	@Override
	public void updateChannel() {
		super.updateChannel();
		//if (!hasListeners) {
			for (IListReader reader : readers){
				handler.updateAndSendList(network, reader, channels, false);
			}
		//}
	}

	public void updateTickLists() {
		super.updateTickLists();
		this.currentList = getChannelsList();
	}

	public @Nullable ChannelList getChannelsList() {
		ChannelList list = new ChannelList(-1, ChannelType.NETWORK_SINGLE, network.getNetworkID());
		for (IListReader monitor : readers) {
			if (monitor instanceof INetworkReader && !monitor.getListenerList().getListeners(ListenerType.FULL_INFO, ListenerType.TEMPORARY).isEmpty()) {
				ChannelList channels = ((INetworkReader) monitor).getChannels();
				list.addAll(channels.getCoords());
				list.addAllUUID(channels.getUUIDs());
			}
		}
		return list;
	}

	@Override
	public boolean isCoordsMonitored(BlockConnection connection) {
		return currentList == null || currentList.isCoordsMonitored(connection.coords);
	}

	@Override
	public boolean isEntityMonitored(EntityConnection connection) {
		return currentList == null || currentList.isEntityMonitored(connection.uuid);
	}

	@Override
	public void onDeleted() {
		super.onDeleted();
		currentList = null;
	}

}
