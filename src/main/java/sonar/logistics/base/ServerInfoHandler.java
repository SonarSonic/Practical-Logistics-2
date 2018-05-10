package sonar.logistics.base;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.PlayerListener;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.base.IInfoManager;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.UniversalChangeableList;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.base.guidance.errors.IInfoError;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.base.listeners.PL2ListenerList;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;
import sonar.logistics.base.utils.worlddata.ConnectedDisplayData;
import sonar.logistics.base.utils.worlddata.GSIData;
import sonar.logistics.core.tiles.displays.DisplayHandler;
import sonar.logistics.core.tiles.displays.DisplayViewerHandler;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayGSISaveHandler;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.types.InfoError;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;
import sonar.logistics.core.tiles.readers.info.handling.InfoHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class ServerInfoHandler implements IInfoManager {

	// server side
	private int IDENTITY_COUNT; // gives unique identity to all PL2 Tiles ( which connect to connections), they can then be retrieved via their identity.
	public Map<ILogicListenable, List<Integer>> changedInfo = new HashMap<>(); // in the form of READER IDENTITY and then CHANGED INFO
	public Map<Integer, DisplayGSI> displays = new HashMap<>();
	private Map<InfoUUID, IInfo> info = new HashMap<>();
	public Map<InfoUUID, AbstractChangeableList> monitoredLists = new HashMap<>();
	public Map<Integer, ILogicListenable> identityTiles = new HashMap<>();
	public Map<Integer, ConnectedDisplay> connectedDisplays = new HashMap<>();

	public static ServerInfoHandler instance() {
		return PL2.proxy.getServerManager();
	}

	public void removeAll() {
		IDENTITY_COUNT = 0;
		changedInfo.clear();
		displays.clear();
		info.clear();
		monitoredLists.clear();
		identityTiles.clear();
		connectedDisplays.clear();
		GSIData.unloadedGSI.clear();
		ConnectedDisplayData.unloadedDisplays.clear();
	}

	public int getNextIdentity() {
		return IDENTITY_COUNT++;
	}

	public int getIdentityCount() {
		return IDENTITY_COUNT;
	}

	/** warning do not use unless reading WorldSavedData, or your world will be corrupted!!!! */
	public int setIdentityCount(int count) {
		return IDENTITY_COUNT = count;
	}

	public boolean enableEvents() {
		return !displays.isEmpty();
	}

	public IInfo getInfoFromUUID(InfoUUID uuid) {
		return info.get(uuid);
	}

	public ConnectedDisplay getConnectedDisplay(int iden) {
		return connectedDisplays.get(iden);
	}

	public void addIdentityTile(ILogicListenable logicTile, PL2AdditionType type) {
		if (identityTiles.containsValue(logicTile)) {
			return;
		}
		identityTiles.put(logicTile.getIdentity(), logicTile);
	}

	public void removeIdentityTile(ILogicListenable logicTile, PL2RemovalType type) {
		identityTiles.remove(logicTile.getIdentity());
	}

	public ILogicListenable getIdentityTile(int iden) {
		return identityTiles.get(iden);
	}

	public void addDisplay(IDisplay display, PL2AdditionType type) {
		DisplayHandler.instance().addDisplay(display, type);
	}

	public void removeDisplay(IDisplay display, PL2RemovalType type) {
		DisplayHandler.instance().removeDisplay(display, type);
	}

	@Override
	public DisplayGSI getGSI(int iden) {
		return displays.get(iden);
	}

	@Nullable
	public Pair<InfoUUID, UniversalChangeableList<?>> getMonitorFromServer(InfoUUID uuid) {
		AbstractChangeableList list = getMonitoredList(uuid);
		return list != null ? new Pair(uuid, list) : null;
	}

	@Nullable
	public <T extends IInfo> AbstractChangeableList getMonitoredList(InfoUUID uuid) {
		return monitoredLists.get(uuid);
	}

	public void sendInfoUpdates() {
		if (!changedInfo.isEmpty() && !displays.isEmpty()) {
			Map<EntityPlayerMP, NBTTagList> savePackets = new HashMap<>();
			for (Entry<ILogicListenable, List<Integer>> id : changedInfo.entrySet()) {
				PL2ListenerList list = id.getKey().getListenerList();
				List<PlayerListener> listeners = list.getAllListeners(ListenerType.OLD_GUI_LISTENER, ListenerType.OLD_DISPLAY_LISTENER);
				if (!listeners.isEmpty()) {
					for (Integer i : id.getValue()) {
						InfoUUID uuid = new InfoUUID(id.getKey().getIdentity(), i);
						IInfo monitorInfo = getInfoFromUUID(uuid);
						if (monitorInfo != null) {
							NBTTagCompound saveTag = InfoHelper.writeInfoToNBT(new NBTTagCompound(), monitorInfo, SyncType.SAVE);
							if (!saveTag.hasNoTags()) {
								saveTag = uuid.writeData(saveTag, SyncType.SAVE);
								InfoPacketHelper.createInfoUpdatesForListeners(savePackets, listeners, saveTag, saveTag, true);
							}
						}
					}
				}
			}
			if (!savePackets.isEmpty()) {// || !syncPackets.isEmpty()) {
				savePackets.forEach((key, value) -> InfoPacketHelper.sendInfoUpdatePacket(key, value, SyncType.SAVE));
				changedInfo.clear();
			}
		}
		return;
	}

	public void sendGSIUpdates(){
		displays.values().forEach(DisplayGSI::doQueuedUpdates);
	}

	public void changeInfo(ILogicListenable source, InfoUUID id, IInfo info) {
		if (!InfoUUID.valid(id)) {
			return;
		}
		IInfo last = getInfoFromUUID(id);
		if (info == null && last != null) {
			info = InfoError.noData;
			return;
		}
		if (InfoHelper.hasInfoChanged(last, info)) {
			setInfo(id, info);
			info.onInfoStored();
			markChanged(source, id);
		}
	}

	public void markChanged(ILogicListenable source, InfoUUID... infoUUIDs) {
		for (InfoUUID id : infoUUIDs) {
			List<Integer> changes = changedInfo.computeIfAbsent(source, FunctionHelper.ARRAY);
			if (!changes.contains(id.channelID)) {
				changes.add(id.channelID);
			}
		}
	}

	@Override
	public Map<Integer, ILogicListenable> getMonitors() {
		return identityTiles;
	}

	@Override
	public Map<InfoUUID, IInfo> getInfoList() {
		return info;
	}

	@Override
	public Map<Integer, ConnectedDisplay> getConnectedDisplays() {
		return connectedDisplays;
	}

	@Override
	public void setInfo(InfoUUID uuid, IInfo newInfo) {
		info.put(uuid, newInfo);
	}

	public List<EntityPlayerMP> getPlayersWatchingUUID(InfoUUID uuid) {
		List<EntityPlayerMP> players = new ArrayList<>();

		this.displays.values().forEach(gsi -> {
			if (gsi.isDisplayingUUID(uuid)) {
				ListHelper.addWithCheck(players, DisplayViewerHandler.instance().getWatchingPlayers(gsi));
			}
		});
		return players;
	}

	public void forEachGSIDisplayingUUID(InfoUUID uuid, Consumer<DisplayGSI> action) {
		this.displays.values().forEach(gsi -> {
			if (gsi.isDisplayingUUID(uuid)) {
				action.accept(gsi);
			}
		});
	}

	//// ERROR HANDLING \\\\

	private List<IInfoError> added_errors = new ArrayList<>();

	public void addError(IInfoError error) {
		added_errors.add(error);
	}

	public void addErrors(List<IInfoError> errors) {
		added_errors.addAll(errors);
	}

	public void sendErrors() {
		if (added_errors.isEmpty()) {
			return;
		}
		Map<DisplayGSI, List<IInfoError>> affectedGSIs = new HashMap<>();
		for (IInfoError error : added_errors) {
			for (DisplayGSI gsi : displays.values()) {
				UUID: for (InfoUUID uuid : error.getAffectedUUIDs()) {
					if (gsi.isDisplayingUUID(uuid)) {
						affectedGSIs.computeIfAbsent(gsi, FunctionHelper.ARRAY);
						affectedGSIs.get(gsi).add(error);
						break UUID;
					}
				}
			}
		}
		for (Entry<DisplayGSI, List<IInfoError>> entry : affectedGSIs.entrySet()) {
			entry.getKey().addInfoErrors(entry.getValue());
			entry.getKey().sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ERRORS);
		}

	}
}