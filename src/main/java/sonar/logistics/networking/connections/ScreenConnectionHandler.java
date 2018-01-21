package sonar.logistics.networking.connections;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.tiles.INetworkConnection;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.helpers.CableHelper;

public class ScreenConnectionHandler extends AbstractConnectionHandler<ILargeDisplay> {

	public static ScreenConnectionHandler instance() {
		return PL2.getDisplayManager();
	}

	//public List<Integer> lockedIDs = Lists.newArrayList();
	public final List<ILargeDisplay> addedScreens = Lists.newArrayList();
	public final List<ILargeDisplay> removedScreens = Lists.newArrayList();

	public void queueDisplayAddition(ILargeDisplay cable) {
		removedScreens.remove(cable);
		addedScreens.add(cable);
	}

	public void queueDisplayRemoval(ILargeDisplay cable) {
		addedScreens.remove(cable);
		removedScreens.add(cable);
	}

	public void tick() {
		Map<Integer, ConnectedDisplay> connected = PL2.getServerManager().getConnectedDisplays();
		if (!connected.isEmpty()) {
			connected.entrySet().forEach(entry -> entry.getValue().update(entry.getKey()));
		}
		addedScreens.forEach(cable -> {
			addConnection(cable);
			cable.updateCableRenders();

		});
		removedScreens.forEach(cable -> {
			removeConnection(cable);
			// cable.updateCableRenders();

		});
		addedScreens.clear();
		removedScreens.clear();
	}
	public int getNextAvailableID() {
		return PL2.getServerManager().getNextIdentity();
	}

	/** abstract connection handler */
	/* @Override public Pair<ConnectableType, Integer> getConnectionType(ILargeDisplay source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) { return CableHelper.getCableConnection(source, world, pos, dir, cableType); } */
	public Pair<ConnectableType, Integer> getConnectionType(ILargeDisplay source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		INetworkConnection connection = CableHelper.getConnection(source, dir, NetworkConnectionType.NETWORK, false);
		if (connection != null && connection instanceof ILargeDisplay && ((ILargeDisplay) connection).getCableFace() == source.getCableFace()) {
			return new Pair(ConnectableType.SCREEN, ((ILargeDisplay) connection).getRegistryID());
		}
		return new Pair(ConnectableType.NONE, -1);
	}

	@Override
	public void onNetworksConnected(int newID, int oldID) {
		ConnectedDisplay screen = PL2.getServerManager().getConnectedDisplays().get(newID);
		PL2.getServerManager().getConnectedDisplays().remove(oldID);
		if (screen != null) {
			screen.setHasChanged();
		} else {
			PL2.logger.error("CONNECTED DISPLAY SCREEN SHOULD NOT BE NULL!");
		}
	}

	@Override
	public void onConnectionAdded(int registryID, ILargeDisplay added) {
		if (registryID != -1) {
			ConnectedDisplay screen = PL2.getServerManager().getConnectedDisplays().get(registryID);
			if (screen == null)
				PL2.getServerManager().getConnectedDisplays().put(registryID, screen = new ConnectedDisplay(added));
			screen.setHasChanged();
		}
	}

	@Override
	public void onConnectionRemoved(int registryID, ILargeDisplay added) {
		PL2.getServerManager().removeDisplay(added);
		if (getConnections(registryID).isEmpty()) {
			PL2.getServerManager().getConnectedDisplays().remove(registryID);
		} else {
			ConnectedDisplay screen = PL2.getServerManager().getConnectedDisplays().get(registryID);
			screen.setHasChanged();
		}
	}

	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {}

	public Pair<ConnectableType, Integer> getConnectionTypeFromObject(ILargeDisplay source, Object connection, EnumFacing dir, ConnectableType cableType) {
		return new Pair(ConnectableType.NONE, -1);
	}

	@Override
	public void addConnectionToNetwork(ILargeDisplay add) {
		addConnection(add);
	}

	@Override
	public void removeConnectionToNetwork(ILargeDisplay remove) {
		addConnection(remove);
	}

}
