package sonar.logistics.networking.info;

import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.networking.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.ChannelList;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.viewers.ListenerType.UpdateType;
import sonar.logistics.networking.common.ListNetworkChannels;

import javax.annotation.Nullable;

public class InfoNetworkChannels extends ListNetworkChannels<IProvidableInfo, InfoNetworkHandler> {

	private ChannelList currentList;

	public InfoNetworkChannels(ILogisticsNetwork network) {
		super(InfoNetworkHandler.INSTANCE, network);
	}

	public void runUpdates() {
		if (UPDATES.canSyncUpdate(UpdateType.GUI)){/// FIXME || ChunkViewerHandler.instance().hasViewersChanged()) {
			updateChannels();
			updateReaders(true);
		}
		if (UPDATES.canSyncUpdate(UpdateType.DISPLAY)) {
			for (IListReader reader : readers) {
				if (reader instanceof INetworkReader && !reader.getListenerList().getDisplayListeners().listener_tallies.isEmpty()) {
					InfoUUID uuid = handler.getReaderUUID(reader);
					AbstractChangeableList<IProvidableInfo> updateList = handler.getUUIDLatestList(uuid);
					((INetworkReader) reader).setMonitoredInfo(updateList, usedChannels.get(reader.getIdentity()), uuid);
				}
			}
		}
		

	}

	public void updateTickLists() {
		super.updateTickLists();
		this.currentList = getChannelsList();
	}

	public @Nullable ChannelList getChannelsList() {
		ChannelList list = new ChannelList(-1, ChannelType.NETWORK_SINGLE, network.getNetworkID());
		for (IListReader monitor : readers) {
			if (monitor instanceof INetworkReader) {
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
