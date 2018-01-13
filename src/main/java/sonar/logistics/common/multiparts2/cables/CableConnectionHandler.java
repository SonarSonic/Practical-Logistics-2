package sonar.logistics.common.multiparts2.cables;

import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.PL2;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.helpers.CableHelper;

public class CableConnectionHandler {

	public static final List<BlockCoords> addedCables = Lists.newArrayList();
	public static final List<BlockCoords> removedCables = Lists.newArrayList();

	public static void queueCableAddition(BlockCoords coords) {
		removedCables.remove(coords);
		addedCables.add(coords);
	}

	public static void queueCableRemoval(BlockCoords coords) {
		addedCables.remove(coords);
		removedCables.add(coords);
	}

	public static void tick() {
		addedCables.forEach(coord -> {
			TileDataCable cable = PL2API.getCableHelper().getCable(coord.getWorld(), coord.getBlockPos());
			if (cable != null) {
				CableConnectionHandler.addCableToNetwork(cable);
				cable.updateCableRenders();
			}
		});
		removedCables.forEach(coord -> {
			TileDataCable cable = PL2API.getCableHelper().getCable(coord.getWorld(), coord.getBlockPos());
			if (cable != null) {
				CableConnectionHandler.removeCableFromNetwork(cable);
				//cable.updateCableRenders();
			}
		});
		addedCables.clear();
		removedCables.clear();
	}

	//// MULTIPARTS \\\\

	public static void onNeighbourMultipartAdded(IDataCable cable, IPartInfo part, IPartInfo otherPart) {
		if (otherPart.getTile() != null && otherPart.getTile() instanceof INetworkTile && otherPart.getSlot() instanceof EnumFaceSlot) {
			onNeighbourAdded(cable, ((INetworkTile) otherPart.getTile()), ((EnumFaceSlot) otherPart.getSlot()).getFacing(), true);
		}
	}

	public static void onNeighbourMultipartRemoved(IDataCable cable, IPartInfo part, IPartInfo otherPart) {
		if (otherPart.getTile() != null && otherPart.getTile() instanceof INetworkTile && otherPart.getSlot() instanceof EnumFaceSlot) {
			onNeighbourRemoved(cable, ((INetworkTile) otherPart.getTile()), ((EnumFaceSlot) otherPart.getSlot()).getFacing(), true);
		}
	}

	//// TILES \\\\

	public static void onNeighbourBlockStateChanged(IDataCable cable, BlockPos pos, BlockPos neighbor) {
		// FIXME - should we check if it can still connect first?
		cable.updateCableRenders();
	}

	public static void onNeighbourTileEntityChanged(IDataCable cable, BlockPos pos, BlockPos neighbor) {
		ILogisticsNetwork network = cable.getNetwork();
		CableHelper.getLocalMonitors(cable).forEach(m -> network.addLocalInfoProvider(m));
		cable.updateCableRenders();
	}

	//// NETWORK TILES \\\\

	public static void onNeighbourAdded(IDataCable cable, INetworkTile networkTile, EnumFacing cableSide, boolean internal) {
		if (cable.canConnect(networkTile.getNetworkID(), cableSide, internal).canConnect()) {
			addConnectionToNetwork(cable, networkTile);
		}
	}

	public static void onNeighbourRemoved(IDataCable cable, INetworkTile networkTile, EnumFacing cableSide, boolean internal) {
		if (networkTile.getNetworkID() == cable.getRegistryID()) {
			removeConnectionFromNetwork(cable, networkTile);
		}
	}

	//// ADD/REMOVE CABLES \\\\

	public static void addCableToNetwork(IDataCable cable) {
		int networkID = PL2.getCableManager().addConnection(cable);
		ILogisticsNetwork network = PL2.getNetworkManager().getOrCreateNetwork(networkID); // create network
		network.onCablesChanged();
	}

	public static void removeCableFromNetwork(IDataCable cable) {
		ILogisticsNetwork network = cable.getNetwork();
		PL2.getCableManager().removeConnection(cable);
		network.onCablesChanged();
	}

	//// ADD/REMOVE CONNECTIONS \\\\

	public static void addConnectionToNetwork(IDataCable cable, INetworkTile tile) {
		ILogisticsNetwork network = cable.getNetwork();
		network.addConnection(tile);
		cable.updateCableRenders();
	}

	public static void removeConnectionFromNetwork(IDataCable cable, INetworkTile tile) {
		cable.getNetwork().removeConnection(tile);
		cable.updateCableRenders();
	}

	/** called only by the logistics network to move connections from network to network */
	public static void addAllConnectionsToNetwork(IDataCable cable, ILogisticsNetwork network) {
		CableHelper.getConnectedTiles(cable).forEach(t -> network.addConnection(t));
		CableHelper.getLocalMonitors(cable).forEach(m -> network.addLocalInfoProvider(m));
	}

	/** called only by the logistics network to move connections from network to network */
	public static void removeAllConnectionsFromNetwork(IDataCable cable, ILogisticsNetwork network) {
		CableHelper.getConnectedTiles(cable).forEach(t -> {
			network.removeConnection(t);
		});
	}

}
