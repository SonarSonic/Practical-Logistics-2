package sonar.logistics.common.multiparts2.cables;

import java.util.List;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import sonar.logistics.PL2;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.helpers.CableHelper;

public class CableConnectionHandler {

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
		// not sure yet
	}

	public static void onNeighbourTileEntityChanged(IDataCable cable, BlockPos pos, BlockPos neighbor) {
		ILogisticsNetwork network = cable.getNetwork();
		CableHelper.getLocalMonitors(cable).forEach(m -> network.addLocalInfoProvider(m));
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
	}

	public static void removeConnectionFromNetwork(IDataCable cable, INetworkTile tile) {
		cable.getNetwork().removeConnection(tile);
	}

	public static void addAllConnectionsToNetwork(IDataCable cable, ILogisticsNetwork network) {
		CableHelper.getConnectedTiles(cable).forEach(t -> network.addConnection(t));
		CableHelper.getLocalMonitors(cable).forEach(m -> network.addLocalInfoProvider(m));
	}

	public static void removeAllConnectionsFromNetwork(IDataCable cable, ILogisticsNetwork network) {
		CableHelper.getConnectedTiles(cable).forEach(t -> {
			network.removeConnection(t);
		});

		// CableHelper.getLocalMonitors(cable).forEach(m -> network.removeLocalInfoProvider(m));
	}

}
