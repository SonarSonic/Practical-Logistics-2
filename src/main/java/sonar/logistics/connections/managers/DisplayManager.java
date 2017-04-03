package sonar.logistics.connections.managers;

import java.util.ArrayList;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.displays.ConnectedDisplayScreen;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.displays.ILargeDisplay;

public class DisplayManager extends AbstractConnectionManager<ILargeDisplay> {

	public ArrayList<Integer> lockedIDs = new ArrayList();

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
		ConnectedDisplayScreen screen = PL2.getServerManager().getConnectedDisplays().get(newID);
		PL2.getServerManager().getConnectedDisplays().remove(oldID);
		if (screen != null) {
			screen.setHasChanged();
		} else
			PL2.logger.error("CONNECTED DISPLAY SCREEN SHOULD NOT BE NULL!");
	}

	@Override
	public void onConnectionAdded(int registryID, ILargeDisplay added) {
		ConnectedDisplayScreen screen = PL2.getServerManager().getConnectedDisplays().get(registryID);
		if (screen == null) {
			PL2.getServerManager().getConnectedDisplays().put(registryID, screen = new ConnectedDisplayScreen(added));
		}
		screen.setHasChanged();
	}

	public Pair<ConnectableType, Integer> getConnectionType(ILargeDisplay source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		IInfoDisplay display = LogisticsAPI.getCableHelper().getDisplayScreen(new BlockCoords(pos.offset(dir)), source.getFace());
		if (display != null && display instanceof ILargeDisplay) {
			ILargeDisplay largeDisplay = (ILargeDisplay) display;
			if (largeDisplay.getFace().equals(source.getFace()) && source.canConnectOnSide(largeDisplay.getRegistryID(), dir.getOpposite()) && largeDisplay.canConnectOnSide(source.getRegistryID(), dir)) {
				return new Pair(ConnectableType.CONNECTION, largeDisplay.getRegistryID());
			}
		}
		return new Pair(ConnectableType.NONE, -1);
	}

	public Pair<ConnectableType, Integer> getConnectionTypeFromObject(ILargeDisplay source, Object connection, EnumFacing dir, ConnectableType cableType) {
		return new Pair(ConnectableType.NONE, -1);
	}

	public void tick() {
		PL2.getServerManager().getConnectedDisplays().entrySet().forEach(entry -> entry.getValue().update(entry.getKey()));
	}

	@Override
	public void onConnectionRemoved(int registryID, ILargeDisplay added) {
		PL2.getServerManager().removeDisplay(added);
		if (this.getConnections(registryID).isEmpty()) {
			PL2.getServerManager().getConnectedDisplays().remove(registryID);
		} else {
			ConnectedDisplayScreen screen = PL2.getServerManager().getConnectedDisplays().get(registryID);
			screen.setHasChanged();
		}

	}

}
