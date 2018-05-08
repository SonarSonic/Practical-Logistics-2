package sonar.logistics.core.tiles.readers.info.handling;

import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.INetworkReader;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.ChannelList;
import sonar.logistics.base.channels.ChannelType;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.handling.ListNetworkChannels;
import sonar.logistics.base.listeners.ListenerType.UpdateType;

import javax.annotation.Nullable;

public class InfoNetworkChannels extends ListNetworkChannels<IProvidableInfo, InfoNetworkHandler> {

	private ChannelList currentList;

	public InfoNetworkChannels(ILogisticsNetwork network) {
		super(InfoNetworkHandler.INSTANCE, network);
	}

	public void runUpdates() {
		if (UPDATES.canSyncUpdate(UpdateType.GUI)){/// FIXME || DisplayViewerHandler.instance().hasViewersChanged()) {
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
