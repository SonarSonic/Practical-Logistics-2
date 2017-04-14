package sonar.logistics.connections.channels;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.tiles.nodes.TransferType;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.helpers.FluidHelper;
import sonar.logistics.helpers.ItemHelper;

//TODO make it possible to have one without a handler that is linked some other wayy
public class TransferNetworkChannels<M extends IInfo, H extends INetworkHandler> extends DefaultNetworkChannels<H> {

	private List<ITransferFilteredTile> nodes = Lists.newArrayList();
	private Iterator<ITransferFilteredTile> nodeIterator;
	private int nodesPerTick = 0;

	public TransferNetworkChannels(H handler, ILogisticsNetwork network) {
		super(handler, network, CacheHandler.TRANSFER_NODES);
	}

	@Override
	public void onCreated() {}

	@Override
	public void onDeleted() {
		super.onDeleted();
		nodes.clear();
		nodeIterator = null;
	}

	protected void updateTicks() {
		super.updateTicks();
		this.nodesPerTick = nodes.size() > handler.updateRate() ? (int) Math.ceil(nodes.size() / Math.max(1, handler.updateRate())) : 1;
		this.nodeIterator = nodes.iterator();
	}

	@Override
	public void updateChannelLists() {
		super.updateChannelLists();
		updateTransferNodes(network.getChannels(CacheType.ALL));
	}

	@Override
	public void addConnection(CacheHandler cache, INetworkListener connection) {
		if (!nodes.contains(connection) && nodes.add((ITransferFilteredTile) connection)) {
			createChannelLists();
			updateTicks();
		}
	}

	@Override
	public void removeConnection(CacheHandler cache, INetworkListener connection) {
		if (nodes.remove(connection)) {
			createChannelLists();
			updateTicks();
		}
	}

	private void updateTransferNodes(List<NodeConnection> allChannels) {
		int used = 0;
		while (nodeIterator.hasNext() && used != nodesPerTick) {
			ITransferFilteredTile transfer = nodeIterator.next();
			BlockConnection connected = transfer.getConnected();
			NodeTransferMode mode = transfer.getTransferMode();
			if (connected == null || mode.isPassive()) {
				continue;
			}
			boolean items = transfer.isTransferEnabled(TransferType.ITEMS);
			boolean fluids = transfer.isTransferEnabled(TransferType.FLUID);
			if (items || fluids) {
				for (NodeConnection connect : allChannels) {
					if (connect != null && connect instanceof BlockConnection && connect.source != connected.source && transfer.getChannels().isMonitored(connect)) {
						if (items)
							ItemHelper.transferItems(mode, connected, (BlockConnection) connect);
						if (fluids)
							FluidHelper.transferFluids(mode, connected, (BlockConnection) connect);
					}
				}
				// TODO entities
			}

			used++;
		}
	}
}
