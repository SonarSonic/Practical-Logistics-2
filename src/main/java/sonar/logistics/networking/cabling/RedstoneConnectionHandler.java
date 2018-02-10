package sonar.logistics.networking.cabling;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.helpers.ListHelper;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IRedstoneCable;
import sonar.logistics.api.cabling.IRedstoneConnectable;

public class RedstoneConnectionHandler extends AbstractConnectionHandler<IRedstoneCable> {

	public static RedstoneConnectionHandler instance() {
		return PL2.instance.redstoneManager;
	}

	public final List<Integer> forUpdate = Lists.newArrayList();
	public final Map<Integer, Integer> powerCache = Maps.newHashMap();
	public final List<IRedstoneCable> addedCables = Lists.newArrayList();
	public final List<IRedstoneCable> removedCables = Lists.newArrayList();

	public final Map<Integer, IRedstoneNetwork> networks = Maps.newHashMap();

	/* public final Map<Integer, IRedstoneConnectable> connectors = Maps.newHashMap(); public final List<IRedstoneConnectable> addedConnectors = Lists.newArrayList(); public final List<IRedstoneConnectable> removedConnectors = Lists.newArrayList(); */
	public void queueCableAddition(IRedstoneCable cable) {
		removedCables.remove(cable);
		addedCables.add(cable);
	}

	public void queueCableRemoval(IRedstoneCable cable) {
		addedCables.remove(cable);
		removedCables.add(cable);
	}

	/* public void queueConnectorAddition(IRedstoneConnectable connection) { removedConnectors.remove(connection); addedConnectors.add(connection); } public void queueConnectorRemoval(IRedstoneConnectable connection) { addedConnectors.remove(connection); removedConnectors.add(connection); } */
	public void removeAll() {
		super.removeAll();
		forUpdate.clear();
		powerCache.clear();
		networks.clear();
	}

	public void tick() {
		addedCables.forEach(cable -> {
			addConnection(cable);
			cable.updateCableRenders();

		});
		removedCables.forEach(cable -> {
			removeConnection(cable);
		});
		networks.forEach((I, N) -> N.tick());
		/* addedConnectors.forEach(connector -> { connectors.put(connector.getIdentity(), connector); }); removedConnectors.forEach(connector -> { connectors.remove(connector.getIdentity()); }); */
		addedCables.clear();
		removedCables.clear();

		if (!forUpdate.isEmpty()) {
			networks.values().forEach(network -> network.updateLocalPower());
			networks.values().forEach(network -> network.updateGlobalPower());
			// networks.values().forEach(network -> network.notifyWatchingNetworksOfChange());
			networks.values().forEach(network -> network.updateActualPower());
			// L//ist<Integer> update = Lists.newArrayList(forUpdate);
			// update.forEach(registryID -> updatePower(registryID));
			forUpdate.clear();
		}
	}

	public void markPowerForUpdate(int registryID) {
		ListHelper.addWithCheck(forUpdate, registryID);
	}

	public int updatePower(int registryID) {
		List<IRedstoneCable> cables = getConnections(registryID);
		if (!cables.isEmpty()) {
			IRedstoneNetwork network = getNetwork(registryID);
			int lastPower = network.getActualPower();
			int power = getNetwork(registryID).updateActualPower();
			if (power != lastPower) {
				powerCache.put(registryID, power);
			}
			cables.forEach(cable -> cable.setNetworkPower(power));
		}
		return 0;
	}

	public IRedstoneNetwork getOrCreateNetwork(int registryID) {
		return networks.computeIfAbsent(registryID, i -> new RedstoneNetwork(registryID));
	}

	public IRedstoneNetwork getNetwork(int registryID) {
		IRedstoneNetwork network = networks.get(registryID);
		return network == null ? EmptyRedstoneNetwork.INSTANCE : network;
	}

	public int getCachedPower(int registryID) {
		return powerCache.getOrDefault(registryID, 0);
	}

	public void onNeighbourBlockStateChanged(IRedstoneCable cable, BlockPos pos, BlockPos neighbor) {
		// getNetwork(cable.getRegistryID()).markCablesChanged();
		markPowerForUpdate(cable.getRegistryID());
		cable.updateCableRenders();
	}

	public void onNeighbourTileEntityChanged(IRedstoneCable cable, BlockPos pos, BlockPos neighbor) {
		// markPowerForUpdate(cable.getRegistryID());
		// cable.updateCableRenders();
	}

	public void onNeighbourMultipartAdded(IRedstoneCable cable, IRedstoneConnectable connect) {
		getNetwork(cable.getRegistryID()).markCablesChanged();
		cable.updateCableRenders();
	}

	public void onNeighbourMultipartRemoved(IRedstoneCable cable, IRedstoneConnectable connect) {
		getNetwork(cable.getRegistryID()).markCablesChanged();
		cable.updateCableRenders();
	}

	public void addAllConnectionsToNetwork(IRedstoneCable cable, IRedstoneNetwork network) {
		RedstoneCableHelper.getConnectables(cable).forEach(t -> network.addConnection(t));
	}

	/** called only by the logistics network to move connections from network to network */
	public void removeAllConnectionsFromNetwork(IRedstoneCable cable, IRedstoneNetwork network) {
		RedstoneCableHelper.getConnectables(cable).forEach(t -> {
			network.removeConnection(t);
		});
	}

	//// ADD/REMOVE CABLES \\\\

	/** abstract connection handler */

	@Override
	public Pair<ConnectableType, Integer> getConnectionType(IRedstoneCable source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		return CableHelper.getCableConnection(source, world, pos, dir, ConnectableType.REDSTONE);
	}

	@Override
	public void onNetworksConnected(int newID, int oldID) {
		getNetwork(newID).markCablesChanged();
		getNetwork(oldID).markCablesChanged();
	}

	@Override
	public void onConnectionAdded(int registryID, IRedstoneCable added) {
		getOrCreateNetwork(registryID).markCablesChanged();
		markPowerForUpdate(registryID);
	}

	@Override
	public void onConnectionRemoved(int registryID, IRedstoneCable added) {
		getNetwork(registryID).markCablesChanged();
		markPowerForUpdate(registryID);
	}

	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {
		for (int i : newNetworks) {
			IRedstoneNetwork network = getNetwork(i);
			network.markCablesChanged();
		}
	}

	@Override
	public void addConnectionToNetwork(IRedstoneCable add) {
		addConnection(add);
	}

	@Override
	public void removeConnectionToNetwork(IRedstoneCable remove) {
		removeConnection(remove);
	}

}
