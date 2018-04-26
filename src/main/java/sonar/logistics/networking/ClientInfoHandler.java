package sonar.logistics.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.core.helpers.NBTHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.readers.ClientLocalProvider;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.api.utils.PL2RemovalType;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.wireless.ClientWirelessEmitter;
import sonar.logistics.networking.events.NetworkPartEvent;

public class ClientInfoHandler implements IInfoManager {

	private Map<Integer, ConnectedDisplay> connectedDisplays = new ConcurrentHashMap<>();
	public Map<Integer, DisplayGSI> displays_gsi = new HashMap<>();
	public Map<Integer, IDisplay> displays_tile = new HashMap<>();

	//received before multipart/tile entity data packets, so it's cached here and then recovered.
	public Map<Integer, NBTTagCompound> invalid_gsi = new HashMap<>();

	public Map<InfoUUID, IInfo> info = new LinkedHashMap<>();

	public Map<Integer, List<Object>> sortedLogicMonitors = new ConcurrentHashMap<>();
	public Map<Integer, List<ClientLocalProvider>> clientLogicMonitors = new ConcurrentHashMap<>();

	public Map<InfoUUID, AbstractChangeableList> changeableLists = new LinkedHashMap<>();
	public Map<Integer, ILogicListenable> identityTiles = new LinkedHashMap<>();
	public Map<Integer, InfoChangeableList> channelMap = new ConcurrentHashMap<>();

	// emitters
	public List<ClientWirelessEmitter> clientDataEmitters = new ArrayList<>();
	public List<ClientWirelessEmitter> clientRedstoneEmitters = new ArrayList<>();

	public static ClientInfoHandler instance() {
		return PL2.proxy.getClientManager();
	}

	@SubscribeEvent
	public void onPartAdded(NetworkPartEvent.AddedPart event) {
		if (event.tile instanceof IDisplay && event.world.isRemote) {
			addDisplay((IDisplay) event.tile, event.type);
		}
	}

	@SubscribeEvent
	public void onPartRemoved(NetworkPartEvent.RemovedPart event) {
		if (event.tile instanceof IDisplay && event.world.isRemote) {
			removeDisplay((IDisplay) event.tile, event.type);
		}
	}

	@Override
	public void removeAll() {
		connectedDisplays.clear();
		displays_gsi.clear();
		displays_tile.clear();
		info.clear();
		sortedLogicMonitors.clear();
		clientLogicMonitors.clear();
		changeableLists.clear();
		identityTiles.clear();
		channelMap.clear();
		clientDataEmitters.clear();
		clientRedstoneEmitters.clear();
		invalid_gsi.clear();
	}

	@Override
	public IInfo getInfoFromUUID(InfoUUID uuid) {
		return info.get(uuid);
	}

	public void setInfo(InfoUUID uuid, IInfo newInfo) {
		info.put(uuid, newInfo);
		onInfoChanged(uuid, newInfo);
	}

	public void addIdentityTile(ILogicListenable infoProvider, PL2AdditionType type) {
		if (identityTiles.containsValue(infoProvider) || infoProvider.getIdentity() == -1) {
			return;
		}
		identityTiles.put(infoProvider.getIdentity(), infoProvider);
	}

	public void removeIdentityTile(ILogicListenable monitor, PL2RemovalType type) {
		identityTiles.remove(monitor.getIdentity());
	}

	public ILogicListenable getIdentityTile(int iden) {
		return identityTiles.get(iden);
	}

	@Override
	public DisplayGSI getGSI(int iden) {
		return displays_gsi.get(iden);
	}

	public void addDisplay(IDisplay display, PL2AdditionType type) {
		if (!displays_tile.containsKey(display.getIdentity())) {
			displays_tile.put(display.getIdentity(), display);
			if(display instanceof ILargeDisplay){
				display = ((ILargeDisplay) display).getConnectedDisplay();
				if(display == null){
					return;
				}
			}
			DisplayGSI gsi = display.getGSI();
			if(gsi == null){
				gsi = new DisplayGSI(display, display.getActualWorld(), display.getInfoContainerID());
				display.setGSI(gsi);
			}
			NBTTagCompound tag = invalid_gsi.get(display.getInfoContainerID());
			if(tag != null){
				gsi.readData(tag, NBTHelper.SyncType.SAVE);
				gsi.validate();
				invalid_gsi.remove(display.getInfoContainerID());
			}
		}
	}

	public void removeDisplay(IDisplay display, PL2RemovalType type) {
		displays_tile.remove(display.getIdentity());
	}

	@Override
	public Map<Integer, ILogicListenable> getMonitors() {
		return identityTiles;
	}

	@Override
	public Map<InfoUUID, IInfo> getInfoList() {
		return info;
	}

	public void onInfoChanged(InfoUUID uuid, IInfo info) {
		for (DisplayGSI display : displays_gsi.values()) {
			if (display.isDisplayingUUID(uuid)) {
				display.onInfoChanged(uuid, info);
			}
		}
	}

	public void onMonitoredListChanged(InfoUUID uuid, AbstractChangeableList list) {
		IInfo info = getInfoFromUUID(uuid);
		for (DisplayGSI display : displays_gsi.values()) {
			if (display.isDisplayingUUID(uuid)) {
				display.onMonitoredListChanged(uuid, list);
			}
		}
	}

	@Nullable
	public <T extends IInfo> AbstractChangeableList getMonitoredList(InfoUUID uuid) {
		for (Entry<InfoUUID, AbstractChangeableList> entry : changeableLists.entrySet()) {
			if (entry.getKey().equals(uuid)) {
				return entry.getValue();
			}
		}
		return UniversalChangeableList.newChangeableList();
	}

	@Override
	public ConnectedDisplay getConnectedDisplay(int iden) {
		return connectedDisplays.get(iden);
	}

	@Override
	public Map<Integer, ConnectedDisplay> getConnectedDisplays() {
		return connectedDisplays;
	}
}
