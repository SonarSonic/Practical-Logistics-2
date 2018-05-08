package sonar.logistics.core.tiles.nodes.transfer.handling;

import sonar.core.helpers.InventoryHelper.DefaultTransferOverride;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.core.tiles.nodes.TransferType;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.channels.handling.DefaultNetworkChannels;
import sonar.logistics.base.filters.ITransferFilteredTile;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.core.tiles.connections.data.network.CacheHandler;
import sonar.logistics.core.tiles.readers.fluids.handling.FluidHelper;
import sonar.logistics.core.tiles.readers.items.handling.ItemHelper;
import sonar.logistics.base.tiles.INetworkTile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//TODO make it possible to have one without a handler that is linked some other wayy
public class TransferNetworkChannels<M extends IInfo, H extends INetworkHandler> extends DefaultNetworkChannels {

	private List<ITransferFilteredTile> nodes = new ArrayList<>();
	private Iterator<ITransferFilteredTile> nodeIterator;
	private int nodesPerTick = 0;
	private DefaultTransferOverride override = new DefaultTransferOverride(128); // make one for the fluids

	public TransferNetworkChannels(ILogisticsNetwork network) {
		super(network, CacheHandler.TRANSFER_NODES);
	}

	@Override
	public int getUpdateRate() {
		return 20;
	}

	@Override
	public void onCreated() {}

	@Override
	public void onDeleted() {
		super.onDeleted();
		nodes.clear();
		nodeIterator = null;
	}

	protected void tickChannels() {
		super.tickChannels();
		this.nodesPerTick = nodes.size() > getUpdateRate() ? (int) Math.ceil(nodes.size() / Math.max(1, getUpdateRate())) : 1;
		this.nodeIterator = nodes.iterator();
		override.reset();
	}

	@Override
	public void updateChannel() {
		super.updateChannel();
		//SimpleProfiler.start("transfer");
		updateTransferNodes(network.getConnections(CacheType.ALL));
		//SimpleProfiler.finishMilli("transfer");
	}

	@Override
	public void addConnection(INetworkTile connection) {
		if (!nodes.contains(connection) && nodes.add((ITransferFilteredTile) connection)) {
			onChannelsChanged();
			tickChannels();
		}
	}

	@Override
	public void removeConnection(INetworkTile connection) {
		if (nodes.remove(connection)) {
			onChannelsChanged();
			tickChannels();
		}
	}

	private void updateTransferNodes(List<NodeConnection> allChannels) {
		int used = 0;
		while (nodeIterator.hasNext() && used != nodesPerTick) {
			override.reset();
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
					if (!override.canTransfer()) {
						break;
					}
					if (connect instanceof BlockConnection && connect.source != connected.source && transfer.getChannels().isMonitored(connect)) {
						if (items)
							ItemHelper.transferItems(mode, connected, (BlockConnection) connect, override);
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
