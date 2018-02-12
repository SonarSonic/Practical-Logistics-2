package sonar.logistics.networking.displays;

import java.util.List;

import sonar.core.api.utils.BlockCoords;
import sonar.logistics.PL2;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;

public class LocalProviderHandler {

	public static boolean updateListenerDisplays;

	/** when a ILogicListenable is added to the world */
	public static void onLocalProviderAdded(ILogicListenable logicTile) {
		updateListenerDisplays = true;
	}

	/** when a ILogicListenable is removed from the world */
	public static void onLocalProviderRemoved(ILogicListenable logicTile) {
		updateListenerDisplays = true;
	}

	/** when a ILogicListenable is added to the world */
	public static void onDisplayAdded(IDisplay display) {
		return;
		/*
		for (int i = 0; i < display.getGSI().getMaxCapacity(); i++) {
			InfoUUID uuid = display.getGSI().getInfoUUID(i);
			if (InfoUUID.valid(uuid)) {
				ILogicListenable listen = PL2.getServerManager().getIdentityTile(uuid.identity);
				if (listen != null) {
					doLocalProviderConnect(display, listen, uuid, i);
					display.getGSI().sendInfoContainerPacketToWatchers();
				}
			}
		}
		*/
	}

	/** when a ILogicListenable is removed from the world */
	public static void onDisplayRemoved(IDisplay display) {
		return;
		/*
		for (int i = 0; i < display.getGSI().getMaxCapacity(); i++) {
			InfoUUID uuid = display.getGSI().getInfoUUID(i);
			if (InfoUUID.valid(uuid)) {
				ILogicListenable listen = PL2.getServerManager().getIdentityTile(uuid.identity);
				if (listen != null) {
					doLocalProviderDisconnect(display, listen, uuid, i);
					display.getGSI().sendInfoContainerPacketToWatchers();
				}
			}
		}
		*/
	}

	public static void doLocalProviderPacket(IDisplay display, InfoUUID uuid, int infoPosition) {
		return;
		/*
		InfoUUID current = display.getGSI().getInfoUUID(infoPosition);
		if (!InfoUUID.valid(current) || !current.equals(uuid)) {
			BlockCoords coords = display.getCoords();
			if (InfoUUID.valid(current)) {
				ILogicListenable listen = PL2.getServerManager().getIdentityTile(current.identity);
				if (listen != null) {
					doLocalProviderDisconnect(display, listen, uuid, infoPosition);
				}
			}
			List<ILogicListenable> logicTiles = DisplayHelper.getLocalProviders(display, coords.getWorld(), coords.getBlockPos());
			boolean connected = logicTiles.stream().anyMatch(tile -> {
				boolean match = tile.getIdentity() == uuid.identity;
				if (match)
					doLocalProviderConnect(display, tile, uuid, infoPosition);
				return match;
			});
		}
		display.getGSI().sendInfoContainerPacketToWatchers();
		*/
	}

	public static void doLocalProviderConnect(IDisplay display, ILogicListenable logicTile, InfoUUID uuid, int infoPosition) {
		return;
		/*
		display.getGSI().setUUID(uuid, infoPosition);
		logicTile.getListenerList().getDisplayListeners().addListener(display, 0);
		PL2.getServerManager().markChanged(logicTile, uuid); // triggers the info packet to be sent to watchers
		*/
	}

	public static void doLocalProviderDisconnect(IDisplay display, ILogicListenable logicTile, InfoUUID uuid, int infoPosition) {
		return;
		/*
		display.getGSI().setUUID(InfoUUID.newEmpty(), infoPosition);
		logicTile.getListenerList().getDisplayListeners().removeListener(display, true, 0); // FIXME does it tally with multiple info watching the same one?
		PL2.getServerManager().markChanged(logicTile, uuid); // triggers the info packet to be sent to watchers
		*/
	}

	/** when a display is connected to a ILogicListenable */
	public static void onLocalProviderConnected(IDisplay display, ILogicListenable logicTile) {
		updateListenerDisplays = true;
	}

	/** when a display is disconnected from a ILogicListenable */
	public static void onLocalProviderDisconnected(IDisplay display, ILogicListenable logicTile) {
		updateListenerDisplays = true;
	}

	public static void updateLists() {
		if (updateListenerDisplays) {
			updateListenerDisplays = false;
			PL2.getServerManager().identityTiles.values().forEach(tile -> tile.getListenerList().getDisplayListeners().clear());
			PL2.getServerManager().displays.values().forEach(display -> display.getGSI().forEachValidUUID(uuid -> {
				ILogicListenable monitor = PL2.getServerManager().getIdentityTile(uuid.getIdentity());
				if (monitor != null && monitor instanceof ILogicListenable) {
					monitor.getListenerList().getDisplayListeners().addListener(display, 0);
				}
			}));
		}
	}
}
