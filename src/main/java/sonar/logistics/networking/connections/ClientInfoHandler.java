package sonar.logistics.networking.connections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.world.World;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.readers.ClientLocalProvider;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.wireless.ClientDataEmitter;

public class ClientInfoHandler implements IInfoManager {

	public Map<Integer, ConnectedDisplay> connectedDisplays = new ConcurrentHashMap<Integer, ConnectedDisplay>();
	public List<IDisplay> displays = Lists.newArrayList();

	// public LinkedHashMap<InfoUUID, IMonitorInfo> lastInfo = Maps.newLinkedHashMap();
	public Map<InfoUUID, IInfo> info = Maps.newLinkedHashMap();

	public Map<Integer, List<Object>> sortedLogicMonitors = new ConcurrentHashMap<Integer, List<Object>>();
	public Map<Integer, List<ClientLocalProvider>> clientLogicMonitors = new ConcurrentHashMap<Integer, List<ClientLocalProvider>>();

	public Map<InfoUUID, AbstractChangeableList> changeableLists = Maps.newLinkedHashMap();
	public Map<Integer, ILogicListenable> identityTiles = Maps.newLinkedHashMap();
	public Map<Integer, InfoChangeableList> channelMap = new ConcurrentHashMap<Integer, InfoChangeableList>();

	// emitters
	public List<ClientDataEmitter> clientEmitters = new ArrayList<ClientDataEmitter>();

	@Override
	public void removeAll() {
		connectedDisplays.clear();
		displays.clear();
		info.clear();
		sortedLogicMonitors.clear();
		clientLogicMonitors.clear();
		changeableLists.clear();
		identityTiles.clear();
		channelMap.clear();
		clientEmitters.clear();
	}

	@Override
	public IInfo getInfoFromUUID(InfoUUID uuid) {
		return info.get(uuid);
	}
	
	public void setInfo(InfoUUID uuid, IInfo newInfo){
		info.put(uuid, newInfo);
		onInfoChanged(uuid, newInfo);
	}

	public void addIdentityTile(ILogicListenable infoProvider) {
		if (identityTiles.containsValue(infoProvider) || infoProvider.getIdentity() == -1) {
			return;
		}
		identityTiles.put(infoProvider.getIdentity(), infoProvider);
	}

	public void removeIdentityTile(ILogicListenable monitor) {
		identityTiles.remove(monitor.getIdentity());
	}

	public ILogicListenable getIdentityTile(int iden) {
		return identityTiles.get(iden);
	}
	
	public void addDisplay(IDisplay display) {
		if (!displays.contains(display)) {
			displays.add(display);
		}
	}

	public void removeDisplay(IDisplay display) {
		displays.remove(display);
	}
	
	@Override
	public Map<Integer,ILogicListenable> getMonitors() {
		return identityTiles;
	}

	@Override
	public Map<InfoUUID, IInfo> getInfoList() {
		return info;
	}

	public void onInfoChanged(InfoUUID uuid, IInfo info){
		for(IDisplay display : displays){
			if(display.container().isDisplayingUUID(uuid)){
				display.container().onInfoChanged(uuid, info);
			}
		}
	}
	
	public void onMonitoredListChanged(InfoUUID uuid, AbstractChangeableList list){
		for(IDisplay display : displays){
			if(display.container().isDisplayingUUID(uuid)){
				display.container().onMonitoredListChanged(uuid, list);
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

	public ConnectedDisplay getOrCreateDisplayScreen(World world, ILargeDisplay display, int registryID) {
		Map<Integer, ConnectedDisplay> displays = getConnectedDisplays();
		ConnectedDisplay toSet = displays.get(registryID);
		if (toSet == null) {
			displays.put(registryID, new ConnectedDisplay(display));
			toSet = displays.get(registryID);
		}
		return toSet;
	}

	@Override
	public Map<Integer, ConnectedDisplay> getConnectedDisplays() {
		return connectedDisplays;
	}
}
