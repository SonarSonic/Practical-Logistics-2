package sonar.logistics.networking.displays;

import java.util.List;

import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.networking.ServerInfoHandler;

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

	public static void doInfoReferenceConnect(DisplayGSI gsi, InfoUUID uuid) {
		BlockCoords coords = gsi.getDisplay().getActualDisplay().getCoords();
		List<ILogicListenable> logicTiles = DisplayHelper.getLocalProviders(gsi.getDisplay(), gsi.getWorld(), coords.getBlockPos());
		boolean connected = logicTiles.stream().anyMatch(tile -> {
			boolean match = tile.getIdentity() == uuid.identity;
			if (match) {				
				doLocalProviderConnect(gsi, tile, uuid);
			}
			return match;
		});
	}

	public static void doInfoReferenceDisconnect(DisplayGSI gsi, InfoUUID uuid) {
		ILogicListenable listen = ServerInfoHandler.instance().getIdentityTile(uuid.identity);
		if (listen != null) {			
			doLocalProviderDisconnect(gsi, listen, uuid);
		}
	}

	public static void doLocalProviderConnect(DisplayGSI display, ILogicListenable logicTile, InfoUUID uuid) {
		 logicTile.getListenerList().getDisplayListeners().addListener(display, 0); 
		 ServerInfoHandler.instance().markChanged(logicTile, uuid); // triggers the info packet to be sent to watchers */
	}

	public static void doLocalProviderDisconnect(DisplayGSI display, ILogicListenable logicTile, InfoUUID uuid) {
		 logicTile.getListenerList().getDisplayListeners().removeListener(display, true, 0); // FIXME does it tally with multiple info watching the same one? 
		 ServerInfoHandler.instance().markChanged(logicTile, uuid); // triggers the info packet to be sent to watchers */
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
			ServerInfoHandler.instance().identityTiles.values().forEach(tile -> tile.getListenerList().getDisplayListeners().clear());
			ServerInfoHandler.instance().displays.values().forEach(gsi -> gsi.forEachValidUUID(uuid -> {
				ILogicListenable monitor = ServerInfoHandler.instance().getIdentityTile(uuid.getIdentity());
				if (monitor != null && monitor instanceof ILogicListenable) {
					monitor.getListenerList().getDisplayListeners().addListener(gsi, 0);
				}
			}));
		}
	}
}
