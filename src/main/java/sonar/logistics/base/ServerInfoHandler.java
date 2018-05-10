package sonar.logistics.base;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.PlayerListener;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.base.guidance.errors.IInfoError;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.base.listeners.PL2ListenerList;
import sonar.logistics.base.utils.worlddata.ConnectedDisplayData;
import sonar.logistics.base.utils.worlddata.GSIData;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayGSISaveHandler;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.types.InfoError;
import sonar.logistics.core.tiles.readers.info.handling.InfoHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ServerInfoHandler extends CommonInfoHandler {

	private int IDENTITY_COUNT; // current count of each tiles unique id
	public Map<ILogicListenable, List<Integer>> changedInfo = new HashMap<>();

	public static ServerInfoHandler instance() {
		return PL2.proxy.getServerManager();
	}

	public ServerInfoHandler() {
		super(Side.SERVER);
	}

	public void removeAll() {
		super.removeAll();
		IDENTITY_COUNT = 0;
		changedInfo.clear();
		GSIData.unloadedGSI.clear();
		ConnectedDisplayData.unloadedDisplays.clear();
	}

	/** warning do not use unless reading WorldSavedData, or your world will be corrupted!!!! */
	public int setIdentityCount(int count) {
		return IDENTITY_COUNT = count;
	}

	public int getNextIdentity() {
		return IDENTITY_COUNT++;
	}

	public int getIdentityCount() {
		return IDENTITY_COUNT;
	}

	public void sendInfoUpdates() {
		if (!changedInfo.isEmpty() && !gsiMap.isEmpty()) {
			Map<EntityPlayerMP, NBTTagList> savePackets = new HashMap<>();
			for (Entry<ILogicListenable, List<Integer>> id : changedInfo.entrySet()) {
				PL2ListenerList list = id.getKey().getListenerList();
				List<PlayerListener> listeners = list.getAllListeners(ListenerType.OLD_GUI_LISTENER, ListenerType.OLD_DISPLAY_LISTENER);
				if (!listeners.isEmpty()) {
					for (Integer i : id.getValue()) {
						InfoUUID uuid = new InfoUUID(id.getKey().getIdentity(), i);
						IInfo monitorInfo = infoMap.get(id);
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

	public void changeInfo(ILogicListenable source, InfoUUID id, IInfo info) {
		if (!InfoUUID.valid(id)) {
			return;
		}
		info = info == null ? InfoError.noData : info;
		IInfo last = infoMap.computeIfAbsent(id, I -> InfoError.noData);
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
			for (DisplayGSI gsi : gsiMap.values()) {
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