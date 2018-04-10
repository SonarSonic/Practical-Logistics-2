package sonar.logistics.networking.cabling;

import java.util.ArrayList;
import java.util.List;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.networking.LogisticsNetworkHandler;

public class CableConnectionHandler extends AbstractConnectionHandler<IDataCable> {

	public static CableConnectionHandler instance() {
		return PL2.proxy.cableManager;
	}

	public final List<IDataCable> addedCables = new ArrayList<>();
	public final List<IDataCable> removedCables = new ArrayList<>();

	public void queueCableAddition(IDataCable cable) {
		removedCables.remove(cable);
		addedCables.add(cable);
	}

	public void queueCableRemoval(IDataCable cable) {
		addedCables.remove(cable);
		removedCables.add(cable);
	}

	public void tick() {
		addedCables.forEach(cable -> {
			addCableToNetwork(cable);

		});
		removedCables.forEach(cable -> {
			removeCableFromNetwork(cable);

		});
		addedCables.clear();
		removedCables.clear();
	}

	//// MULTIPARTS \\\\

	public void onNeighbourMultipartAdded(IDataCable cable, IPartInfo part, IPartInfo otherPart) {
		if (otherPart.getTile() != null && otherPart.getTile() instanceof INetworkTile && otherPart.getSlot() instanceof EnumFaceSlot) {
			onNeighbourAdded(cable, ((INetworkTile) otherPart.getTile()), ((EnumFaceSlot) otherPart.getSlot()).getFacing(), true);
		}
	}

	public void onNeighbourMultipartRemoved(IDataCable cable, IPartInfo part, IPartInfo otherPart) {
		if (otherPart.getTile() != null && otherPart.getTile() instanceof INetworkTile && otherPart.getSlot() instanceof EnumFaceSlot) {
			onNeighbourRemoved(cable, ((INetworkTile) otherPart.getTile()), ((EnumFaceSlot) otherPart.getSlot()).getFacing(), true);
		}
	}

	//// TILES \\\\

	public void onNeighbourBlockStateChanged(IDataCable cable, BlockPos pos, BlockPos neighbor) {
		//cable.updateCableRenders();
	}

	public void onNeighbourTileEntityChanged(IDataCable cable, BlockPos pos, BlockPos neighbor) {
		ILogisticsNetwork network = cable.getNetwork();
		CableHelper.getLocalMonitors(cable).forEach(m -> network.addLocalInfoProvider(m));
		//cable.updateCableRenders();
	}

	//// NETWORK TILES \\\\

	public void onNeighbourAdded(IDataCable cable, INetworkTile networkTile, EnumFacing cableSide, boolean internal) {
		if (cable.canConnect(networkTile.getNetworkID(), cable.getConnectableType(), cableSide, internal).canConnect()) {
			addConnectionToNetwork(cable, networkTile);
		}
	}

	public void onNeighbourRemoved(IDataCable cable, INetworkTile networkTile, EnumFacing cableSide, boolean internal) {
		if (networkTile.getNetworkID() == cable.getRegistryID()) {
			removeConnectionFromNetwork(cable, networkTile);
		}
	}

	//// ADD/REMOVE CABLES \\\\

	public void addCableToNetwork(IDataCable cable) {
		int networkID = addConnection(cable);
		ILogisticsNetwork network = LogisticsNetworkHandler.instance().getOrCreateNetwork(networkID); // create network
		network.onCablesChanged();
	}

	public void removeCableFromNetwork(IDataCable cable) {
		ILogisticsNetwork network = cable.getNetwork();
		removeConnection(cable);
		network.onCablesChanged();
	}

	//// ADD/REMOVE CONNECTIONS \\\\

	public void addConnectionToNetwork(IDataCable cable, INetworkTile tile) {
		ILogisticsNetwork network = cable.getNetwork();
		network.addConnection(tile);
		//cable.updateCableRenders();
	}

	public void removeConnectionFromNetwork(IDataCable cable, INetworkTile tile) {
		cable.getNetwork().removeConnection(tile);
		//cable.updateCableRenders();
	}

	/** called only by the logistics network to move connections from network to network */
	public void addAllConnectionsToNetwork(IDataCable cable, ILogisticsNetwork network) {
		CableHelper.getConnectedTiles(cable).forEach(t -> network.addConnection(t));
		CableHelper.getLocalMonitors(cable).forEach(m -> network.addLocalInfoProvider(m));
	}

	/** called only by the logistics network to move connections from network to network */
	public void removeAllConnectionsFromNetwork(IDataCable cable, ILogisticsNetwork network) {
		CableHelper.getConnectedTiles(cable).forEach(t -> {
			network.removeConnection(t);
		});
	}

	/** abstract connection handler */

	@Override
	public Pair<ConnectableType, Integer> getConnectionType(IDataCable source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		return CableHelper.getCableConnection(source, world, pos, dir, cableType);
	}

	@Override
	public void onNetworksConnected(int newID, int oldID) {
		NetworkManager().connectNetworks(oldID, newID);
	}

	@Override
	public void onConnectionAdded(int registryID, IDataCable added) {
		addAllConnectionsToNetwork(added, NetworkManager().getOrCreateNetwork(registryID));
	}

	@Override
	public void onConnectionRemoved(int id, IDataCable added) {
		ILogisticsNetwork network = NetworkManager().getNetwork(id);
		removeAllConnectionsFromNetwork(added, network);
		network.removeConnections(); // ensure the CacheHandler empty the parts to remove.
	}

	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {
		for (int i : newNetworks) {
			ILogisticsNetwork network = NetworkManager().getNetwork(i);
			network.onCablesChanged();
		}
	}

	@Override
	public void addConnectionToNetwork(IDataCable add) {
		addCableToNetwork(add);
	}

	@Override
	public void removeConnectionToNetwork(IDataCable remove) {
		removeCableFromNetwork(remove);
	}

}
