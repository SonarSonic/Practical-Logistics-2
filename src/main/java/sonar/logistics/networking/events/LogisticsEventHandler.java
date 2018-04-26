package sonar.logistics.networking.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.core.helpers.ListHelper;
import sonar.core.utils.IWorldTile;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.cabling.IRedstoneCable;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.nodes.IEntityNode;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.api.utils.PL2RemovalType;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.networking.LogisticsNetwork;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.NetworkHelper;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.cabling.CableConnectionHandler;
import sonar.logistics.networking.cabling.RedstoneConnectionHandler;
import sonar.logistics.networking.cabling.WirelessDataManager;
import sonar.logistics.networking.cabling.WirelessRedstoneManager;
import sonar.logistics.networking.displays.ChunkViewerHandler;
import sonar.logistics.networking.displays.ConnectedDisplayChange;
import sonar.logistics.networking.displays.DisplayHandler;
import sonar.logistics.networking.displays.LocalProviderHandler;
import sonar.logistics.networking.displays.LocalProviderHandler.UpdateCause;

public class LogisticsEventHandler {

	public static LogisticsEventHandler instance() {
		return PL2.proxy.eventHandler;
	}

	public Map<NetworkChanges, List<LogisticsNetwork>> network_changes = new LinkedHashMap<>();
	public Map<Class, IQueueHandler> queue_handlers = new LinkedHashMap<>();
	public Map<Class, Map<Object, PL2AdditionType>> queued_additions = new LinkedHashMap<>();
	public Map<Class, Map<Object, PL2RemovalType>> queued_removals = new LinkedHashMap<>();
	public EventScheduler CONSTRUCTING = new EventScheduler();
	public EventScheduler UPDATING = new EventScheduler();
	public EventScheduler NOTIFYING = new EventScheduler();

	public interface IQueueHandler<T> {
		void flushQueue(Map<T, PL2AdditionType> added, Map<T, PL2RemovalType> removed);
	}

	{
		for (NetworkChanges type : NetworkChanges.values()) {
			network_changes.put(type, new ArrayList<>());
		}
	}

	{
		queue_handlers.put(IDataCable.class, this::flushDataCables);
		queue_handlers.put(IRedstoneCable.class, this::flushRedstoneCables);
		queue_handlers.put(ILargeDisplay.class, this::flushLargeDisplays);
		queue_handlers.put(IDisplay.class, this::flushDisplays);
		queue_handlers.put(INetworkTile.class, this::flushDefaultParts);

		queue_handlers.keySet().forEach(clazz -> {
			queued_additions.put(clazz, new HashMap<>());
			queued_removals.put(clazz, new HashMap<>());
		});
	}

	public void triggerConstructingPhase() {
		queue_handlers.keySet().forEach(TYPE -> {
			Map added = queued_additions.get(TYPE);
			Map removed = queued_removals.get(TYPE);
			queue_handlers.get(TYPE).flushQueue(added, removed);
			added.clear();
			removed.clear();
		});
		doNetworkChanges();
		RedstoneConnectionHandler.instance().tick();
		LogisticsNetworkHandler.instance().tick();
		
		CONSTRUCTING.flushEvents();
	}

	public void triggerUpdatingPhase() {
		LocalProviderHandler.updateLocalProviderConnections();
		DisplayHandler.instance().updateConnectedDisplays();
		ChunkViewerHandler.instance().updateDisplayViewers();
		
		UPDATING.flushEvents();
	}

	public void triggerNotifyingPhase() {
		ServerInfoHandler.instance().sendInfoUpdates();
		CableConnectionHandler.instance().doRenderUpdates();
		ServerInfoHandler.instance().sendErrors();
		WirelessDataManager.instance().sendDataEmittersToListeners();
		WirelessRedstoneManager.instance().sendDataEmittersToListeners();
		ServerInfoHandler.instance().sendGSIUpdates();
		NOTIFYING.flushEvents();
	}

	public void queueNetworkChange(ILogisticsNetwork network, NetworkChanges... updates) {
		if (!network.isValid()) {
			return;
		}
		for (NetworkChanges update : updates) {
			ListHelper.addWithCheck(network_changes.get(update), (LogisticsNetwork) network);
		}
	}

	public void queueNetworkAddition(Object tile, PL2AdditionType type) {
		if (tile instanceof IWorldTile && ((IWorldTile) tile).getActualWorld().isRemote) {
			return;
		}
		queue_handlers.keySet().forEach(clazz -> {
			if (clazz.isInstance(tile)) {
				queued_removals.get(clazz).remove(tile);
				PL2AdditionType current = queued_additions.get(clazz).get(tile);
				if (current == null || current.ordinal() > type.ordinal()) {
					queued_additions.get(clazz).put(tile, type);
				}
			}
		});
	}

	public void queueNetworkRemoval(Object tile, PL2RemovalType type) {
		if (tile instanceof IWorldTile && ((IWorldTile) tile).getActualWorld().isRemote) {
			return;
		}
		queue_handlers.keySet().forEach(clazz -> {
			if (clazz.isInstance(tile)) {
				queued_additions.get(clazz).remove(tile);
				PL2RemovalType current = queued_removals.get(clazz).get(tile);
				if (current == null || current.ordinal() > type.ordinal()) {
					queued_removals.get(clazz).put(tile, type);
				}
			}
		});
	}

	public void flushDataCables(Map<IDataCable, PL2AdditionType> added, Map<IDataCable, PL2RemovalType> removed) {
		added.forEach((cable, type) -> MinecraftForge.EVENT_BUS.post(new NetworkCableEvent.AddedCable(cable, cable.getActualWorld(), type)));
		removed.forEach((cable, type) -> MinecraftForge.EVENT_BUS.post(new NetworkCableEvent.RemovedCable(cable, cable.getActualWorld(), type)));
		CableConnectionHandler.instance().doQueuedUpdates();
	}

	public void flushRedstoneCables(Map<IRedstoneCable, PL2AdditionType> added, Map<IRedstoneCable, PL2RemovalType> removed) {
		added.forEach((cable, type) -> MinecraftForge.EVENT_BUS.post(new NetworkCableEvent.AddedCable(cable, cable.getActualWorld(), type)));
		removed.forEach((cable, type) -> MinecraftForge.EVENT_BUS.post(new NetworkCableEvent.RemovedCable(cable, cable.getActualWorld(), type)));
	}

	public void flushLargeDisplays(Map<ILargeDisplay, PL2AdditionType> added, Map<ILargeDisplay, PL2RemovalType> removed) {
		if (!added.isEmpty() || !removed.isEmpty()) {
			DisplayHandler.instance().rebuild.clear();
			added.forEach((display, type) -> DisplayHandler.instance().onDisplayAddition(display));
			removed.forEach((display, type) -> DisplayHandler.instance().onDisplayRemoval(display));
			DisplayHandler.instance().createConnectedDisplays();
		}
	}

	public void flushDisplays(Map<IDisplay, PL2AdditionType> added, Map<IDisplay, PL2RemovalType> removed) {
		if (!added.isEmpty() || !removed.isEmpty()) {
			added.forEach((display, type) -> ServerInfoHandler.instance().addDisplay(display, type));
			removed.forEach((display, type) -> ServerInfoHandler.instance().removeDisplay(display, type));
		}
	}

	public <T extends INetworkTile> void flushDefaultParts(Map<T, PL2AdditionType> added, Map<T, PL2RemovalType> removed) {
		added.forEach((tile, type) -> MinecraftForge.EVENT_BUS.post(new NetworkPartEvent.AddedPart(tile, tile.getActualWorld(), type)));
		removed.forEach((tile, type) -> MinecraftForge.EVENT_BUS.post(new NetworkPartEvent.RemovedPart(tile, tile.getActualWorld(), type)));
	}

	//// NETWORK UPDATES \\\\

	public void doNetworkChanges() {
		for (Entry<NetworkChanges, List<LogisticsNetwork>> entry : network_changes.entrySet()) {
			entry.getKey().network.performUpdates(entry.getValue());
			entry.getValue().clear();
		}
	}

	public void updateLocalProviders(List<LogisticsNetwork> networks) {
		// mark packets to be sent to watchers?
		List<LogisticsNetwork> watching = network_changes.get(NetworkChanges.GLOBAL_PROVIDERS);
		ListHelper.addWithCheck(watching, networks);
		networks.forEach(network -> {
			// info providers have already been added by cables.
			List<ILogisticsNetwork> watchers = NetworkHelper.getAllNetworks(network, ILogisticsNetwork.WATCHING_NETWORK);
			watchers.stream().filter(ILogisticsNetwork::isValid).forEach(N -> ListHelper.addWithCheck(watching, (LogisticsNetwork) N));

		});
	}

	public void updateLocalChannels(List<LogisticsNetwork> networks) {
		// mark packets to be sent to watchers?
		List<LogisticsNetwork> watching = network_changes.get(NetworkChanges.GLOBAL_CHANNELS);
		ListHelper.addWithCheck(watching, networks);
		networks.forEach(network -> {
			network.createLocalChannels();
			List<ILogisticsNetwork> watchers = NetworkHelper.getAllNetworks(network, ILogisticsNetwork.WATCHING_NETWORK);
			watchers.stream().filter(ILogisticsNetwork::isValid).forEach(N -> ListHelper.addWithCheck(watching, (LogisticsNetwork) N));
		});
	}

	public void updateGlobalProviders(List<LogisticsNetwork> networks) {
		networks.forEach(LogisticsNetwork::createGlobalProviders);
	}

	public void updateGlobalChannels(List<LogisticsNetwork> networks) {
		networks.forEach(LogisticsNetwork::createGlobalChannels);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPartAdded(NetworkPartEvent.AddedPart event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof ILogicListenable) {
				ServerInfoHandler.instance().addIdentityTile((ILogicListenable) event.tile, event.type);
				LocalProviderHandler.queueUpdate((ILogicListenable) event.tile, UpdateCause.getCause(event.type));
			}
			if (event.tile instanceof ILargeDisplay) {
				DisplayHandler.instance().markConnectedDisplayChanged(((ILargeDisplay) event.tile).getRegistryID(), ConnectedDisplayChange.SUB_NETWORK_CHANGED);
			} else if (event.tile instanceof IDisplay) {
				((IDisplay) event.tile).getGSI().validateAllInfoReferences();
			}
			if (event.type == PL2AdditionType.PLAYER_ADDED) {
				event.tile.onTileAddition();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPartRemoved(NetworkPartEvent.RemovedPart event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof ILogicListenable) {
				ServerInfoHandler.instance().removeIdentityTile((ILogicListenable) event.tile, event.type);
				LocalProviderHandler.queueUpdate((ILogicListenable) event.tile, UpdateCause.getCause(event.type));
			}
			if (event.tile instanceof ILargeDisplay) {
				DisplayHandler.instance().markConnectedDisplayChanged(((ILargeDisplay) event.tile).getRegistryID(), ConnectedDisplayChange.SUB_NETWORK_CHANGED);
			} else if (event.tile instanceof IDisplay) {
				((IDisplay) event.tile).getGSI().validateAllInfoReferences();
			}
			if (event.type == PL2RemovalType.PLAYER_REMOVED) {
				event.tile.onTileRemoval();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onNetworksConnected(NetworkEvent.ConnectedNetwork event) {
		PL2.logger.info("Networks Connected: " + event.network.getNetworkID() + " " + event.connected_network.getNetworkID());
		List<IInfoProvider> tiles = event.connected_network.getGlobalInfoProviders();
		tiles.forEach(t -> LocalProviderHandler.queueUpdate(t, UpdateCause.NETWORK_CHANGE));

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onNetworksDisconnected(NetworkEvent.DisconnectedNetwork event) {
		PL2.logger.info("Networks Disconnected: " + event.network.getNetworkID() + " " + event.disconnected_network.getNetworkID());
		List<IInfoProvider> tiles = event.disconnected_network.getGlobalInfoProviders();
		tiles.forEach(t -> LocalProviderHandler.queueUpdate(t, UpdateCause.NETWORK_CHANGE));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTileConnected(NetworkEvent.ConnectedTile event) {
		PL2.logger.info("Tile Connected: " + event.tile.getIdentity() + " " + event.tile);
		event.network.addConnection(event.tile);
		event.tile.onNetworkConnect(event.network);

		if (event.tile instanceof INode || event.tile instanceof IEntityNode) {
			queueNetworkChange(event.network, NetworkChanges.LOCAL_CHANNELS);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTileDisconnected(NetworkEvent.DisconnectedTile event) {

		PL2.logger.info("Tile Disconnected: " + event.tile.getIdentity() + " " + event.tile);
		event.network.removeConnection(event.tile);
		event.tile.onNetworkDisconnect(event.network);

		if (event.tile instanceof INode || event.tile instanceof IEntityNode) {
			queueNetworkChange(event.network, NetworkChanges.LOCAL_CHANNELS);
		}

	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onWirelessTileConnected(NetworkEvent.ConnectedTile event) {
		if (event.tile instanceof IDataReceiver) {
			WirelessDataManager.instance().connectReceiver(event.network, (IDataReceiver) event.tile);
		}
		if (event.tile instanceof IDataEmitter) {
			WirelessDataManager.instance().connectEmitter(event.network, (IDataEmitter) event.tile);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onWirelessTileDisconnected(NetworkEvent.DisconnectedTile event) {
		if (event.tile instanceof IDataReceiver) {
			WirelessDataManager.instance().disconnectReceiver(event.network, (IDataReceiver) event.tile);
		}
		if (event.tile instanceof IDataEmitter) {
			WirelessDataManager.instance().disconnectEmitter(event.network, (IDataEmitter) event.tile);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLocalProviderConnected(NetworkEvent.ConnectedLocalProvider event) {
		PL2.logger.info("Local Provider Connected: " + event.tile.getIdentity() + " " + event.tile);
		event.network.addLocalInfoProvider(event.tile);
		queueNetworkChange(event.network, NetworkChanges.LOCAL_PROVIDERS);

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLocalProviderDisconnected(NetworkEvent.DisconnectedLocalProvider event) {
		PL2.logger.info("Local Provider Disconnected: " + event.tile.getIdentity() + " " + event.tile);
		event.network.removeLocalInfoProvider(event.tile);
		queueNetworkChange(event.network, NetworkChanges.LOCAL_PROVIDERS);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCableAdded(NetworkCableEvent.AddedCable event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof IDataCable) {
				CableConnectionHandler.instance().addConnectionToNetwork((IDataCable) event.tile);
			}
			if (event.tile instanceof IRedstoneCable) {
				RedstoneConnectionHandler.instance().addConnectionToNetwork((IRedstoneCable) event.tile);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCableRemoved(NetworkCableEvent.RemovedCable event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof IDataCable) {
				CableConnectionHandler.instance().removeConnectionFromNetwork((IDataCable) event.tile);
			}
			if (event.tile instanceof IRedstoneCable) {
				RedstoneConnectionHandler.instance().removeConnectionFromNetwork((IRedstoneCable) event.tile);
			}
		}

	}
}
