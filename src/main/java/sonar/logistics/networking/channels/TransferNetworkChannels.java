package sonar.logistics.networking.channels;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import sonar.core.helpers.InventoryHelper.DefaultTransferOverride;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.tiles.nodes.TransferType;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.helpers.FluidHelper;
import sonar.logistics.helpers.ItemHelper;
import sonar.logistics.networking.CacheHandler;

//TODO make it possible to have one without a handler that is linked some other wayy
public class TransferNetworkChannels<M extends IInfo, H extends INetworkHandler> extends DefaultNetworkChannels {

	private List<ITransferFilteredTile> nodes = Lists.newArrayList();
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

	protected void updateTicks() {
		super.updateTicks();
		this.nodesPerTick = nodes.size() > getUpdateRate() ? (int) Math.ceil(nodes.size() / Math.max(1, getUpdateRate())) : 1;
		this.nodeIterator = nodes.iterator();
		override.reset();
	}

	@Override
	public void updateChannel() {
		super.updateChannel();
		updateTransferNodes(network.getConnections(CacheType.ALL));
	}

	@Override
	public void addConnection(CacheHandler cache, INetworkListener connection) {
		if (!nodes.contains(connection) && nodes.add((ITransferFilteredTile) connection)) {
			onChannelsChanged();
			updateTicks();
		}
	}

	@Override
	public void removeConnection(CacheHandler cache, INetworkListener connection) {
		if (nodes.remove(connection)) {
			onChannelsChanged();
			updateTicks();
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
					if (connect != null && connect instanceof BlockConnection && connect.source != connected.source && transfer.getChannels().isMonitored(connect)) {
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
