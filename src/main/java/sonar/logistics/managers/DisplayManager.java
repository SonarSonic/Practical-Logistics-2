package sonar.logistics.managers;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;

public class DisplayManager extends AbstractConnectionManager<ILargeDisplay> {

	public List<Integer> lockedIDs = Lists.newArrayList();

	public int getNextAvailableID() {
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			if (!lockedIDs.contains(i)) {
				if (i < connections.size()) {
					if ((connections.get(i) == null || connections.get(i).isEmpty() || connections.get(i).size() == 0)) {
						return i;
					}
				} else {
					return i;
				}
			}
		}
		return -1; // impossible
	}

	@Override
	public void onNetworksConnected(int newID, int oldID) {
		ConnectedDisplay screen = PL2.getServerManager().getConnectedDisplays().get(newID);
		PL2.getServerManager().getConnectedDisplays().remove(oldID);
		if (screen != null) {
			screen.setHasChanged();
		} else
			PL2.logger.error("CONNECTED DISPLAY SCREEN SHOULD NOT BE NULL!");
	}

	@Override
	public void onConnectionAdded(int registryID, ILargeDisplay added) {
		ConnectedDisplay screen = PL2.getServerManager().getConnectedDisplays().get(registryID);
		if (screen == null) {
			PL2.getServerManager().getConnectedDisplays().put(registryID, screen = new ConnectedDisplay(added));
		}
		screen.setHasChanged();
	}

	public Pair<ConnectableType, Integer> getConnectionType(ILargeDisplay source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		IDisplay display = LogisticsAPI.getCableHelper().getDisplayScreen(new BlockCoords(pos.offset(dir)), source.getCableFace());
		if (display != null && display instanceof ILargeDisplay) {
			ILargeDisplay largeDisplay = (ILargeDisplay) display;
			if (largeDisplay.getCableFace().equals(source.getCableFace()) && source.canConnectOnSide(largeDisplay.getRegistryID(), dir.getOpposite(), false) && largeDisplay.canConnectOnSide(source.getRegistryID(), dir, false)) {
				return new Pair(ConnectableType.CONNECTABLE, largeDisplay.getRegistryID());
			}
		}
		return new Pair(ConnectableType.NONE, -1);
	}

	public Pair<ConnectableType, Integer> getConnectionTypeFromObject(ILargeDisplay source, Object connection, EnumFacing dir, ConnectableType cableType) {
		return new Pair(ConnectableType.NONE, -1);
	}

	public void tick() {
		Map<Integer, ConnectedDisplay> connected = PL2.getServerManager().getConnectedDisplays();
		if (!connected.isEmpty())
			connected.entrySet().forEach(entry -> entry.getValue().update(entry.getKey()));
	}

	@Override
	public void onConnectionRemoved(int registryID, ILargeDisplay added) {
		PL2.getServerManager().removeDisplay(added);
		if (this.getConnections(registryID).isEmpty()) {
			PL2.getServerManager().getConnectedDisplays().remove(registryID);
		} else {
			ConnectedDisplay screen = PL2.getServerManager().getConnectedDisplays().get(registryID);
			screen.setHasChanged();
		}
	}

	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {
	}

}
