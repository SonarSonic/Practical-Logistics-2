package sonar.logistics.core.tiles.connections.redstone.network;

import com.google.common.collect.Lists;
import sonar.core.helpers.ListHelper;
import sonar.core.listener.ListenableList;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneCable;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneConnectable;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstonePowerProvider;
import sonar.logistics.api.core.tiles.connections.redstone.network.IRedstoneNetwork;
import sonar.logistics.api.core.tiles.wireless.emitters.IRedstoneEmitter;
import sonar.logistics.api.core.tiles.wireless.receivers.IRedstoneReceiver;
import sonar.logistics.core.tiles.connections.redstone.handling.RedstoneConnectionHandler;
import sonar.logistics.core.tiles.connections.redstone.handling.RedstoneConnectionHelper;
import sonar.logistics.core.tiles.wireless.handling.WirelessRedstoneManager;

import java.util.ArrayList;
import java.util.List;

public class RedstoneNetwork implements IRedstoneNetwork {

	public final List<IRedstonePowerProvider> providers = new ArrayList<>(); // local methods
	public final List<IRedstoneReceiver> receivers = new ArrayList<>(); // receivers
	public final List<IRedstoneEmitter> emitters = new ArrayList<>(); // emitters
	public final ListenableList<IRedstoneNetwork> subNetworks = new ListenableList(this, 2);
	public final List<IRedstoneConnectable> toAdd = new ArrayList<>();
	public final List<IRedstoneConnectable> toRemove = new ArrayList<>();
	public int registryID = -1;
	public int localPower = 0, globalPower = 0, actualPower = 0;
	public boolean cablesChanged = true;

	public RedstoneNetwork(int registryID) {
		this.registryID = registryID;
	}

	public int getNetworkID() {
		return registryID;
	}

	@Override
	public void markCablesChanged() {
		cablesChanged = true;
	}

	@Override
	public boolean doCablesNeedUpdate() {
		return cablesChanged;
	}

	public void tick() {
		toAdd.forEach(this::doAddConnection);
		toRemove.forEach(this::doRemoveConnection);
		if (doCablesNeedUpdate()) {
			updateCables();
		}

	}

	public void updateCables() {
		Lists.newArrayList(providers).forEach(this::removeConnection);
		Lists.newArrayList(receivers).forEach(this::removeConnection);
		Lists.newArrayList(emitters).forEach(this::removeConnection);
		List<IRedstoneCable> cables = RedstoneConnectionHandler.instance().getConnections(registryID);
		cables.forEach(cable -> RedstoneConnectionHelper.getConnectables(cable).forEach(this::addConnection));
		
		cablesChanged = false;
	}

	@Override
	public void addConnection(IRedstoneConnectable connectable) {
		ListHelper.addWithCheck(toAdd, connectable);
		toRemove.remove(connectable);
	}

	@Override
	public void removeConnection(IRedstoneConnectable connectable) {
		ListHelper.addWithCheck(toRemove, connectable);
		toAdd.remove(connectable);
	}

	public void doAddConnection(IRedstoneConnectable connection) {
		if (connection instanceof IRedstoneReceiver) {
			ListHelper.addWithCheck(receivers, (IRedstoneReceiver) connection);
			WirelessRedstoneManager.instance().connectReceiver(this, (IRedstoneReceiver) connection);
		} else if (connection instanceof IRedstoneEmitter) {
			ListHelper.addWithCheck(emitters, (IRedstoneEmitter) connection);
			WirelessRedstoneManager.instance().connectEmitter(this, (IRedstoneEmitter) connection);
		} else if (connection instanceof IRedstonePowerProvider) {
			ListHelper.addWithCheck(providers, (IRedstonePowerProvider) connection);
		}
		connection.onNetworkConnect(this);
		RedstoneConnectionHandler.instance().markPowerForUpdate(registryID);
	}

	public void doRemoveConnection(IRedstoneConnectable connection) {
		if (connection instanceof IRedstoneReceiver) {
			receivers.remove(connection);
			WirelessRedstoneManager.instance().disconnectReceiver(this, (IRedstoneReceiver) connection);
		} else if (connection instanceof IRedstoneEmitter) {
			emitters.remove(connection);
			WirelessRedstoneManager.instance().disconnectEmitter(this, (IRedstoneEmitter) connection);
		} else if (connection instanceof IRedstonePowerProvider) {
			providers.remove(connection);
		}
		connection.onNetworkDisconnect(this);
		RedstoneConnectionHandler.instance().markPowerForUpdate(registryID);
	}

	@Override
	public int getActualPower() {
		return actualPower;
	}

	@Override
	public int getLocalPower() {
		return localPower;
	}

	@Override
	public int getGlobalPower() {
		return globalPower;
	}

	@Override
	public int updateActualPower() {
		int newPower = getLocalPower();
		if (newPower == 0) {
			newPower = getGlobalPower();
		}
		if (newPower != this.actualPower) {
			this.actualPower = newPower;
			notifyWatchingNetworksOfChange();
			RedstoneConnectionHandler.instance().powerCache.put(registryID, actualPower);
		}
		List<IRedstoneCable> cables = RedstoneConnectionHandler.instance().getConnections(registryID);
		cables.forEach(cable -> cable.setNetworkPower(actualPower)); // we set it regardless of if it changed, as may be new connections
		return actualPower;

	}

	@Override
	public int updateLocalPower() {
		int newPower = 0;
		for (IRedstonePowerProvider provider : providers) {
			newPower = provider.getCurrentPower();
			if (newPower > 0) {
				break;
			}
		}
		return localPower = newPower;
	}

	@Override
	public int updateGlobalPower() {
		int newPower = 0;
		for (IRedstoneReceiver receiver : receivers) {
			receiver.updatePower();
			newPower = receiver.getRedstonePower();
			if (newPower > 0) {
				break;
			}
		}
		return globalPower = newPower;
	}

	public void notifyWatchingNetworksOfChange() {
		List<IRedstoneNetwork> networks = getAllNetworks(this, IRedstoneNetwork.WATCHING_NETWORK);
		for (IRedstoneNetwork network : networks) {
			if (network != this)
				network.onNetworkPowerChanged(this);
		}
	}

	@Override
	public void onNetworkPowerChanged(IRedstoneNetwork network) {
		if (isActive(Math.max(getLocalPower(), getGlobalPower())) != isActive(Math.max(network.getLocalPower(), network.getGlobalPower()))) {
			updateGlobalPower();
		}
	}

	public static List<IRedstoneNetwork> getAllNetworks(IRedstoneNetwork network, int networkType) {
		List<IRedstoneNetwork> networks = new ArrayList<>();
		addSubNetworks(networks, network, networkType);
		return networks;
	}

	public static void addSubNetworks(List<IRedstoneNetwork> building, IRedstoneNetwork network, int networkType) {
		building.add(network);
		List<IRedstoneNetwork> subNetworks = network.getListenerList().getListeners(networkType);
		for (IRedstoneNetwork sub : subNetworks) {
			if (sub.isValid() && !building.contains(sub)) {
				addSubNetworks(building, sub, networkType);
			}
		}
	}

	/** just to check if it's more than 0, quick and dirty */
	public boolean isActive(int power) {
		return power > 0;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public ListenableList<IRedstoneNetwork> getListenerList() {
		return subNetworks;
	}

}
