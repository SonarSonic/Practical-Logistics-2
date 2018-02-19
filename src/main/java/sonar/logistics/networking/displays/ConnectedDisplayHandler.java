package sonar.logistics.networking.displays;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.ICableConnectable;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.networking.cabling.AbstractConnectionHandler;
import sonar.logistics.networking.cabling.CableHelper;
import sonar.logistics.packets.PacketConnectedDisplayRemove;
import sonar.logistics.packets.PacketConnectedDisplayUpdate;
import stanhebben.zenscript.annotations.NotNull;

public class ConnectedDisplayHandler extends AbstractConnectionHandler<ILargeDisplay> {

	public static ConnectedDisplayHandler instance() {
		return PL2.instance.displayManager;
	}

	// public List<Integer> lockedIDs = Lists.newArrayList();
	public final List<ILargeDisplay> addedScreens = Lists.newArrayList();
	public final List<ILargeDisplay> removedScreens = Lists.newArrayList();
	public final List<Integer> fullRemovals = Lists.newArrayList(); // identities of displays which were removed by the player.
	public final List<Integer> trackedIDs = Lists.newArrayList();
	public final Map<Integer, List<ConnectedDisplayChange>> connectedDisplaysChanged = Maps.newHashMap();

	public enum ConnectedDisplayChange {
		SUB_DISPLAY_CHANGED(ConnectedDisplayHandler::updateLargeDisplays), // LARGE DISPLAY ADDED/DISCONNECTED
		SUB_NETWORK_CHANGED(ConnectedDisplayHandler::updateConnectedNetworks), // LARGE DISPLAYS CONNECTS/DISCONNECTS FROM A NETWORK
		WATCHERS_CHANGED(ConnectedDisplayHandler::updateWatchers); // WHEN PEOPLE WATCHING THIS DISPLAY HAVE CHANGED

		/** returns if the rest of the updates should continue */
		public ChangeLogic changeLogic;

		ConnectedDisplayChange(ChangeLogic changeLogic) {
			this.changeLogic = changeLogic;
		}

		public boolean shouldRunChange(List<ConnectedDisplayChange> changes) {
			if (changes.contains(this)) {
				return true;
			}
			switch (this) {
			case SUB_DISPLAY_CHANGED:
				break;
			case SUB_NETWORK_CHANGED:
				break;
			case WATCHERS_CHANGED:
				break;
			default:
				break;
			}
			return false;
		}

		/** you may add to the list of current changes, but it will only run ones preceding this in the order of the enum, using ConnectedDisplayHandler.markConnectedDisplayChanged will work the same */
		public boolean doChange(List<ConnectedDisplayChange> changes, ConnectedDisplay change) {
			return changeLogic.doChange(changes, change);
		}

		public static interface ChangeLogic {

			public boolean doChange(List<ConnectedDisplayChange> changes, ConnectedDisplay change);
		}

	}

	public void removeAll() {
		super.removeAll();
		addedScreens.clear();
		removedScreens.clear();
		fullRemovals.clear();
		trackedIDs.clear();
	}

	public void queueDisplayAddition(ILargeDisplay display) {
		removedScreens.remove(display);
		addedScreens.add(display);
	}

	public void queueDisplayRemoval(ILargeDisplay display) {
		addedScreens.remove(display);
		removedScreens.add(display);
	}

	/** we must have a system for full removals so Connected Displays can be deleted, otherwise we do not know if */
	public void markFullDisplayRemoval(ILargeDisplay display) {
		fullRemovals.add(display.getIdentity());
	}

	public void markConnectedDisplayChanged(int registryID, @NotNull ConnectedDisplayChange... changes) {
		connectedDisplaysChanged.putIfAbsent(registryID, Lists.newArrayList());
		for (ConnectedDisplayChange change : changes) {
			if (!connectedDisplaysChanged.get(registryID).contains(change)) {
				connectedDisplaysChanged.get(registryID).add(change);
			}
		}
	}

	public List<ConnectedDisplayChange> getChanges(int registryID) {
		return connectedDisplaysChanged.getOrDefault(registryID, Lists.newArrayList());
	}

	public void tick() {
		addedScreens.forEach(screen -> {
			addConnection(screen);
			if (!trackedIDs.contains(screen.getIdentity())) {
				trackedIDs.add(screen.getIdentity());
			}
			screen.updateCableRenders();

		});
		removedScreens.forEach(screen -> {
			removeConnection(screen);
			if (fullRemovals.contains(screen.getIdentity())) {
				trackedIDs.remove(screen.getIdentity());
			}

		});
		addedScreens.clear();
		removedScreens.clear();
		Map<Integer, ConnectedDisplay> connected = PL2.getServerManager().getConnectedDisplays();
		if (!connected.isEmpty()) {
			connected.values().forEach(this::runChanges);
		}
		fullRemovals.clear();
		connectedDisplaysChanged.clear();
	}

	public void runChanges(ConnectedDisplay display) {
		List<ConnectedDisplayChange> changes = getChanges(display.getRegistryID());
		for (ConnectedDisplayChange change : ConnectedDisplayChange.values()) { // keeps order, and allows other changes to be during another
			if (change.shouldRunChange(changes) && !change.doChange(changes, display)) {
				return;
			}
		}
	}

	public static boolean updateLargeDisplays(List<ConnectedDisplayChange> changes, ConnectedDisplay display) {
		List<ILargeDisplay> displays = ConnectedDisplayHandler.instance().getConnections(display.getRegistryID());
		if (!displays.isEmpty()) {
			display.setDisplayScaling();
			ConnectedDisplayHandler.instance().markConnectedDisplayChanged(display.getRegistryID(), ConnectedDisplayChange.WATCHERS_CHANGED, ConnectedDisplayChange.SUB_NETWORK_CHANGED);
			return true;
		}
		// FIXME - no displays are loaded, therefore, we either mark as being UNLOADED, OR WE DELETE ...

		return false; // if the count = 0 don't continue updates.

	}

	public static boolean updateConnectedNetworks(List<ConnectedDisplayChange> changes, ConnectedDisplay display) {
		// FIXME check infocontainer can still connect to all the info.
		return true;
	}

	public static boolean updateWatchers(List<ConnectedDisplayChange> changes, ConnectedDisplay display) {
		if (display.getCoords() != null) { // this shouldn't happen, but if it does it'll break the ChunkViewerHandler
			List<EntityPlayerMP> watchers = ChunkViewerHandler.instance().getWatchingPlayers(display);
			watchers.forEach(watcher -> PL2.network.sendTo(new PacketConnectedDisplayUpdate(display, display.getRegistryID()), watcher));
		}
		return true;
	}

	public static void setDisplayLocking(int registryID, boolean locked) {
		ConnectedDisplay display = PL2.getServerManager().getConnectedDisplay(registryID);
		if (display != null) {
			display.isLocked.setObject(locked);
			ConnectedDisplayHandler.instance().getConnections(registryID).forEach(d -> d.setLocked(locked));
		}
	}

	public int getNextAvailableID() {
		return PL2.getServerManager().getNextIdentity();
	}

	/** abstract connection handler */
	/* @Override public Pair<ConnectableType, Integer> getConnectionType(ILargeDisplay source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) { return CableHelper.getCableConnection(source, world, pos, dir, cableType); } */
	public Pair<ConnectableType, Integer> getConnectionType(ILargeDisplay source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		ICableConnectable connection = CableHelper.getConnection(source, dir, CableConnectionType.NETWORK, false);
		if (connection != null && connection instanceof ILargeDisplay && ((ILargeDisplay) connection).getCableFace() == source.getCableFace()) {
			return new Pair(ConnectableType.SCREEN, ((ILargeDisplay) connection).getRegistryID());
		}
		return new Pair(ConnectableType.NONE, -1);
	}

	@Override
	public void onNetworksConnected(int newID, int oldID) {
		ConnectedDisplay screen = PL2.getServerManager().getConnectedDisplays().get(newID);
		PL2.getServerManager().getConnectedDisplays().remove(oldID);
		markConnectedDisplayChanged(oldID, ConnectedDisplayChange.SUB_DISPLAY_CHANGED);

		if (screen != null) {
			markConnectedDisplayChanged(newID, ConnectedDisplayChange.SUB_DISPLAY_CHANGED);
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
			markConnectedDisplayChanged(registryID, ConnectedDisplayChange.SUB_DISPLAY_CHANGED);
		}
	}

	@Override
	public void onConnectionRemoved(int registryID, ILargeDisplay added) {
		PL2.getServerManager().removeDisplay(added);
		if (getConnections(registryID).isEmpty()) {
			PL2.getServerManager().getConnectedDisplays().remove(registryID);

			List<EntityPlayerMP> players = ChunkViewerHandler.instance().getWatchingPlayers(added);
			players.forEach(listener -> PL2.network.sendTo(new PacketConnectedDisplayRemove(registryID), listener));
		} else {
			ConnectedDisplay screen = PL2.getServerManager().getConnectedDisplays().get(registryID);
			markConnectedDisplayChanged(registryID, ConnectedDisplayChange.SUB_DISPLAY_CHANGED);
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
