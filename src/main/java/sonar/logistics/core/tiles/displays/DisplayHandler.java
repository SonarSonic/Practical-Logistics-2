package sonar.logistics.core.tiles.displays;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;
import sonar.logistics.api.core.tiles.connections.ICableConnectable;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.api.core.tiles.displays.tiles.ILargeDisplay;
import sonar.logistics.api.core.tiles.displays.tiles.ISmallDisplay;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;
import sonar.logistics.base.utils.worlddata.GSIData;
import sonar.logistics.core.tiles.connections.data.handling.AbstractConnectionHandler;
import sonar.logistics.core.tiles.connections.data.handling.CableConnectionHelper;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayGSISaveHandler;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplayChange;
import sonar.logistics.core.tiles.displays.tiles.holographic.TileAbstractHolographicDisplay;
import sonar.logistics.network.packets.PacketConnectedDisplayUpdate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayHandler extends AbstractConnectionHandler<ILargeDisplay> {

	public static DisplayHandler instance() {
		return PL2.proxy.server_display_manager;
	}

	public final Map<Integer, List<ConnectedDisplayChange>> display_updates = new HashMap<>();
	public final Map<Integer, TileAbstractHolographicDisplay> holographic_displays = new HashMap<>();
	public List<Integer> rebuild = Lists.newArrayList();

	@Override
	public void removeAll(){
		super.removeAll();
		rebuild.clear();
		display_updates.clear();
		holographic_displays.clear();
	}

	public void updateConnectedDisplays() {
		if (!display_updates.isEmpty()) {
			Map<Integer, ConnectedDisplay> connected = ServerInfoHandler.instance().getConnectedDisplays();
			if (!connected.isEmpty()) {
				connected.values().forEach(this::runChanges);
			}
			display_updates.clear();
		}
	}

	public void onDisplayAddition(ILargeDisplay display) {
		ListHelper.addWithCheck(rebuild, display.getRegistryID());
		addConnectionToNetwork(display);
		ListHelper.addWithCheck(rebuild, display.getRegistryID());
	}

	public void onDisplayRemoval(ILargeDisplay display) {
		ListHelper.addWithCheck(rebuild, display.getRegistryID());
		removeConnectionFromNetwork(display);
		ListHelper.addWithCheck(rebuild, display.getRegistryID());
	}

	public void addDisplay(IDisplay display, PL2AdditionType type){
	    if(display instanceof ISmallDisplay) {
			ISmallDisplay screen = (ISmallDisplay) display;
			screen.setGSI(new DisplayGSI(display, display.getActualWorld(), display.getInfoContainerID()));
            NBTTagCompound tag = GSIData.unloadedGSI.get(display.getInfoContainerID());
            if(tag != null){
                screen.getGSI().readData(tag, NBTHelper.SyncType.SAVE);
            }
        }
        DisplayGSI gsi = display.getGSI();
        if (gsi != null && gsi.getDisplay() != null && !ServerInfoHandler.instance().gsiMap.containsValue(gsi)) {
            validateGSI(display, gsi);
        }
    }

    public void removeDisplay(IDisplay display, PL2RemovalType type){
        if(display instanceof ISmallDisplay) {
			ISmallDisplay screen = (ISmallDisplay) display;
            if(type == PL2RemovalType.PLAYER_REMOVED){
                GSIData.unloadedGSI.remove(display.getInfoContainerID());
            }else if(display.getGSI() != null){
                GSIData.unloadedGSI.put(display.getInfoContainerID(), display.getGSI().writeData(new NBTTagCompound(), NBTHelper.SyncType.SAVE));
            }
        }
        DisplayGSI gsi = display.getGSI();
        if (gsi != null && gsi.getDisplay() != null) {
            invalidateGSI(display, gsi);
        }
    }

    public static void addClientDisplay(IDisplay display, PL2AdditionType type){
		if (!ClientInfoHandler.instance().displays_tile.containsKey(display.getIdentity())) {
			ClientInfoHandler.instance().displays_tile.put(display.getIdentity(), display);
			if(display instanceof ILargeDisplay){
				display = ((ILargeDisplay) display).getConnectedDisplay().orElse(null);
				if(display == null){
					return;
				}
			}
			DisplayGSI gsi = display.getGSI();
			if(gsi == null){
				gsi = new DisplayGSI(display, display.getActualWorld(), display.getInfoContainerID());
				display.setGSI(gsi);
			}
			NBTTagCompound tag = ClientInfoHandler.instance().invalid_gsi.get(display.getInfoContainerID());
			if(tag != null){
				gsi.readData(tag, NBTHelper.SyncType.SAVE);
				gsi.validate();
				ClientInfoHandler.instance().invalid_gsi.remove(display.getInfoContainerID());
			}
		}
	}


	public static void removeClientDisplay(IDisplay display, PL2RemovalType type) {
		IDisplay current = ClientInfoHandler.instance().displays_tile.get(display.getIdentity());
		if(current == display){
			ClientInfoHandler.instance().displays_tile.remove(display.getIdentity());
		}
	}

    public void validateGSI(IDisplay display, DisplayGSI gsi) {
        if (display == gsi.getDisplay().getActualDisplay()) {
            gsi.validate();
            gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ALL_DATA);
			DisplayViewerHandler.instance().onDisplayAdded(gsi);
        }
    }

    public void invalidateGSI(IDisplay display, DisplayGSI gsi) {
        gsi.invalidate();
		DisplayViewerHandler.instance().onDisplayRemoved(gsi);
    }

	public void createConnectedDisplays() {
		for (Integer i : rebuild) {
			ConnectedDisplay display = ServerInfoHandler.instance().getConnectedDisplays().get(i);
			List<ILargeDisplay> displays = getConnections(i);
			if (displays.isEmpty()) {
				if (display != null) {
					invalidateGSI(display, display.getGSI());
				}
				ServerInfoHandler.instance().getConnectedDisplays().remove(i);
			} else if (display == null) {
				ILargeDisplay first_display = displays.get(0);
				World world = first_display.getActualWorld();
				ConnectedDisplay connectedDisplay = ConnectedDisplay.loadDisplay(world, i);
				ServerInfoHandler.instance().getConnectedDisplays().put(i, connectedDisplay);
				connectedDisplay.face.setObject(first_display.getCableFace());
				connectedDisplay.setDisplayScaling();
				connectedDisplay.getGSI().validate();
				markConnectedDisplayChanged(i, ConnectedDisplayChange.SUB_NETWORK_CHANGED, ConnectedDisplayChange.SUB_DISPLAY_CHANGED, ConnectedDisplayChange.WATCHERS_CHANGED);
			} else {
				markConnectedDisplayChanged(i, ConnectedDisplayChange.SUB_DISPLAY_CHANGED, ConnectedDisplayChange.WATCHERS_CHANGED);
			}
		}
	}

	public void markConnectedDisplayChanged(int registryID, @Nonnull ConnectedDisplayChange... changes) {
		display_updates.putIfAbsent(registryID, new ArrayList<>());
		for (ConnectedDisplayChange change : changes) {
			if (!display_updates.get(registryID).contains(change)) {
				display_updates.get(registryID).add(change);
			}
		}
	}

	public List<ConnectedDisplayChange> getChanges(int registryID) {
		return display_updates.getOrDefault(registryID, new ArrayList<>());
	}

	public void runChanges(ConnectedDisplay display) {
		List<ConnectedDisplayChange> changes = getChanges(display.getRegistryID());
		for (ConnectedDisplayChange change : ConnectedDisplayChange.values()) { // keeps order, and allows other changes to be during another
			if (change.shouldRunChange(changes) && !change.doChange(changes, display)) {
				changes.remove(change);
				return;
			}
		}
	}

	public static boolean updateLargeDisplays(List<ConnectedDisplayChange> changes, ConnectedDisplay display) {
		List<ILargeDisplay> displays = DisplayHandler.instance().getConnections(display.getRegistryID());
		if (!displays.isEmpty()) {
			display.setDisplayScaling();
			DisplayHandler.instance().markConnectedDisplayChanged(display.getRegistryID(), ConnectedDisplayChange.WATCHERS_CHANGED, ConnectedDisplayChange.SUB_NETWORK_CHANGED);
			return true;
		}
		// FIXME - no base are loaded, therefore, we either mark as being UNLOADED, OR WE DELETE ...

		return false; // if the count = 0 don't continue updates.

	}

	public static boolean updateConnectedNetworks(List<ConnectedDisplayChange> changes, ConnectedDisplay display) {
		display.getGSI().validateAllInfoReferences();
		return true;
	}

	public static boolean updateWatchers(List<ConnectedDisplayChange> changes, ConnectedDisplay display) {
		if (display.getCoords() != null) { // this shouldn't happen, but if it does it'll break the DisplayViewerHandler
			List<EntityPlayerMP> watchers = DisplayViewerHandler.instance().getWatchingPlayers(display.getGSI());
			watchers.forEach(watcher -> PL2.network.sendTo(new PacketConnectedDisplayUpdate(display, display.getRegistryID()), watcher));
		}
		return true;
	}

	public int getNextAvailableID() {
		return ServerInfoHandler.instance().getNextIdentity();
	}

	/** abstract connection handler */
	public Pair<EnumCableConnectionType, Integer> getConnectionType(ILargeDisplay source, World world, BlockPos pos, EnumFacing dir, EnumCableConnectionType cableType) {
		ICableConnectable connection = CableConnectionHelper.getConnection(source, dir, EnumCableConnection.NETWORK, false);
		if (connection instanceof ILargeDisplay && ((ILargeDisplay) connection).getCableFace() == source.getCableFace()) {
			return new Pair(EnumCableConnectionType.SCREEN, ((ILargeDisplay) connection).getRegistryID());
		}
		return new Pair(EnumCableConnectionType.NONE, -1);
	}

	@Override
	public void onNetworksConnected(int newID, int oldID) {
		ConnectedDisplay screen = ServerInfoHandler.instance().getConnectedDisplays().get(newID);
		ServerInfoHandler.instance().getConnectedDisplays().remove(oldID);
		markConnectedDisplayChanged(oldID, ConnectedDisplayChange.SUB_DISPLAY_CHANGED);

		if (screen != null) {
			markConnectedDisplayChanged(newID, ConnectedDisplayChange.SUB_DISPLAY_CHANGED);
		} else {
			PL2.logger.error("CONNECTED DISPLAY SCREEN SHOULD NOT BE NULL!");
		}
	}

	@Override
	public void onConnectionAdded(int registryID, ILargeDisplay added) {
		markConnectedDisplayChanged(registryID, ConnectedDisplayChange.SUB_DISPLAY_CHANGED);
	}

	@Override
	public void onConnectionRemoved(int registryID, ILargeDisplay added) {
		markConnectedDisplayChanged(registryID, ConnectedDisplayChange.SUB_DISPLAY_CHANGED);
	}

	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {}

	public Pair<EnumCableConnectionType, Integer> getConnectionTypeFromObject(ILargeDisplay source, Object connection, EnumFacing dir, EnumCableConnectionType cableType) {
		return new Pair(EnumCableConnectionType.NONE, -1);
	}

	@Override
	public void addConnectionToNetwork(ILargeDisplay add) {
		addConnection(add);
	}

	@Override
	public void removeConnectionFromNetwork(ILargeDisplay remove) {
		removeConnection(remove);
	}

}
