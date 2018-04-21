package sonar.logistics.networking.events;

import java.util.List;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.IRedstoneCable;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.api.utils.PL2RemovalType;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.networking.CacheHandler;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.cabling.CableConnectionHandler;
import sonar.logistics.networking.cabling.RedstoneConnectionHandler;
import sonar.logistics.networking.displays.ConnectedDisplayChange;
import sonar.logistics.networking.displays.DisplayHandler;
import sonar.logistics.networking.displays.LocalProviderHandler;
import sonar.logistics.networking.displays.LocalProviderHandler.UpdateCause;

public class LogisticsEventHandler {

	public static void registerHandlers() {
		MinecraftForge.EVENT_BUS.register(LogisticsEventHandler.class);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPartAdded(NetworkPartEvent.AddedPart event) {
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
	public static void onPartRemoved(NetworkPartEvent.RemovedPart event) {
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
	public static void onNetworksConnected(NetworkEvent.ConnectedNetwork event) {
		List<IInfoProvider> tiles = event.connected_network.getGlobalInfoProviders();
		tiles.forEach(t -> LocalProviderHandler.queueUpdate(t, UpdateCause.NETWORK_CHANGE));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onNetworksDisconnected(NetworkEvent.DisconnectedNetwork event) {
		List<IInfoProvider> tiles = event.disconnected_network.getGlobalInfoProviders();
		tiles.forEach(t -> LocalProviderHandler.queueUpdate(t, UpdateCause.NETWORK_CHANGE));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onCableAdded(NetworkCableEvent.AddedCable event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof IDataCable) {
				CableConnectionHandler.instance().queueCableAddition((IDataCable) event.tile);
			}
			if (event.tile instanceof IRedstoneCable) {
				RedstoneConnectionHandler.instance().queueCableAddition((IRedstoneCable) event.tile);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onCableRemoved(NetworkCableEvent.RemovedCable event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof IDataCable) {
				CableConnectionHandler.instance().queueCableRemoval((IDataCable) event.tile);
			}
			if (event.tile instanceof IRedstoneCable) {
				RedstoneConnectionHandler.instance().queueCableRemoval((IRedstoneCable) event.tile);
			}
		}

	}
}
