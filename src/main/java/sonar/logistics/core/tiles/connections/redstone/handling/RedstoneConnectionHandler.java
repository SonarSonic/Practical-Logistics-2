package sonar.logistics.core.tiles.connections.redstone.handling;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.helpers.ListHelper;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneCable;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneConnectable;
import sonar.logistics.api.core.tiles.connections.redstone.network.IRedstoneNetwork;
import sonar.logistics.core.tiles.connections.data.handling.AbstractConnectionHandler;
import sonar.logistics.core.tiles.connections.data.handling.CableConnectionHelper;
import sonar.logistics.core.tiles.connections.redstone.network.EmptyRedstoneNetwork;
import sonar.logistics.core.tiles.connections.redstone.network.RedstoneNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedstoneConnectionHandler extends AbstractConnectionHandler<IRedstoneCable> {

	public static RedstoneConnectionHandler instance() {
		return PL2.proxy.redstoneManager;
	}

	public final List<Integer> forUpdate = new ArrayList<>();
	public final Map<Integer, Integer> powerCache = new HashMap<>();
	public final Map<Integer, IRedstoneNetwork> networks = new HashMap<>();
	private int REDSTONE_NETWORK_COUNT;

	/* public void queueConnectorAddition(IRedstoneConnectable connection) { removedConnectors.remove(connection); addedConnectors.add(connection); } public void queueConnectorRemoval(IRedstoneConnectable connection) { addedConnectors.remove(connection); removedConnectors.add(connection); } */
	public void removeAll() {
		super.removeAll();
		REDSTONE_NETWORK_COUNT = 0;
		forUpdate.clear();
		powerCache.clear();
		networks.clear();
	}

	@Override
	public int getNextAvailableID() {
		return REDSTONE_NETWORK_COUNT++;
	}

	public int getCurrentIdentity() {
		return REDSTONE_NETWORK_COUNT;
	}

	/** warning do not use unless reading WorldSavedData, or your world will be corrupted!!!! */
	public int setIdentityCount(int count) {
		return REDSTONE_NETWORK_COUNT = count;
	}


	public void tick() {
		networks.forEach((I, N) -> N.tick());

		if (!forUpdate.isEmpty()) {
			networks.values().forEach(IRedstoneNetwork::updateLocalPower);
			networks.values().forEach(IRedstoneNetwork::updateGlobalPower);
			// connections.values().forEach(handling -> handling.notifyWatchingNetworksOfChange());
			networks.values().forEach(IRedstoneNetwork::updateActualPower);
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
		cable.updateCableRenders();
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
		RedstoneConnectionHelper.getConnectables(cable).forEach(network::addConnection);
	}

	/** called only by the logistics handling to move connections from handling to handling */
	public void removeAllConnectionsFromNetwork(IRedstoneCable cable, IRedstoneNetwork network) {
		RedstoneConnectionHelper.getConnectables(cable).forEach(network::removeConnection);
	}

	//// ADD/REMOVE CABLES \\\\

	/** abstract connection handler */

	@Override
	public Pair<EnumCableConnectionType, Integer> getConnectionType(IRedstoneCable source, World world, BlockPos pos, EnumFacing dir, EnumCableConnectionType cableType) {
		return CableConnectionHelper.getCableConnection(source, world, pos, dir, EnumCableConnectionType.REDSTONE);
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
	public void removeConnectionFromNetwork(IRedstoneCable remove) {
		removeConnection(remove);
	}

}
