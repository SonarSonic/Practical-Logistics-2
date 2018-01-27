package sonar.logistics.networking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.PlayerListener;
import sonar.core.utils.Pair;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.networking.displays.ChunkViewerHandler;
import sonar.logistics.networking.info.InfoHelper;

public class ServerInfoHandler implements IInfoManager {

	// server side
	private int IDENTITY_COUNT; // gives unique identity to all PL2 Tiles ( which connect to networks), they can then be retrieved via their identity.
	// public List<InfoUUID> changedInfo = Lists.newArrayList();
	public Map<ILogicListenable, List<Integer>> changedInfo = Maps.newHashMap(); // in the form of READER IDENTITY and then CHANGED INFO
	public Map<Integer, IDisplay> displays = Maps.newHashMap();
	public boolean markDisplaysDirty = true;
	private Map<InfoUUID, IInfo> info = Maps.newHashMap();
	public Map<InfoUUID, AbstractChangeableList> monitoredLists = Maps.newHashMap();
	public Map<Integer, ILogicListenable> identityTiles = Maps.newHashMap();
	public Map<Integer, ConnectedDisplay> connectedDisplays = Maps.newHashMap();
	// public Map<Integer, DisplayInteractionEvent> clickEvents = Maps.newHashMap();
	public Map<Integer, List<ChunkPos>> chunksToUpdate = Maps.newHashMap();

	public boolean newChunks = false;

	public int ticks;
	public boolean updateListenerDisplays;

	public void removeAll() {
		changedInfo.clear();
		displays.clear();
		info.clear();
		monitoredLists.clear();
		identityTiles.clear();
		connectedDisplays.clear();
		// clickEvents.clear();
		chunksToUpdate.clear();
		IDENTITY_COUNT = 0;
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

	public ConnectedDisplay getOrCreateDisplayScreen(World world, ILargeDisplay display, int registryID) {
		if (registryID != -1) {
			Map<Integer, ConnectedDisplay> displays = getConnectedDisplays();
			ConnectedDisplay toSet = displays.get(registryID);
			if (toSet == null) {
				displays.put(registryID, new ConnectedDisplay(display));
				toSet = displays.get(registryID);
			}
			return toSet;
		}
		return new ConnectedDisplay(display);// kills Ait
	}

	public void addIdentityTile(ILogicListenable infoProvider) {
		if (identityTiles.containsValue(infoProvider)) {
			return;
		}
		identityTiles.put(infoProvider.getIdentity(), infoProvider);
		updateListenerDisplays = true;
	}

	public void removeIdentityTile(ILogicListenable monitor) {
		for (int i = 0; i < (monitor instanceof IInfoProvider ? ((IInfoProvider) monitor).getMaxInfo() : 1); i++) {
			info.put(new InfoUUID(monitor.getIdentity(), i), InfoError.noData);
		}
		identityTiles.remove(monitor.getIdentity());
		updateListenerDisplays = true;
	}

	public ILogicListenable getIdentityTile(int iden) {
		return identityTiles.get(iden);
	}

	public void addDisplay(IDisplay display) {
		if (!displays.containsValue(display)) {
			displays.put(display.getIdentity(), display);
			ChunkViewerHandler.instance().onDisplayAdded(display);
			updateListenerDisplays = true;
		}
	}

	public void removeDisplay(IDisplay display) {
		identityTiles.remove(display);
		ChunkViewerHandler.instance().onDisplayRemoved(display);
		updateListenerDisplays = true;
	}

	public IDisplay getDisplay(int iden) {
		return displays.get(iden);
	}

	public void addChangedChunk(int dimension, ChunkPos chunkPos) {
		List<ChunkPos> chunks = chunksToUpdate.computeIfAbsent(dimension, FunctionHelper.ARRAY);
		if (!chunks.contains(chunkPos)) {
			chunks.add(chunkPos);
			newChunks = true;
		}
	}

	@Nullable
	public Pair<InfoUUID, UniversalChangeableList<?>> getMonitorFromServer(InfoUUID uuid) {
		for (Entry<InfoUUID, ?> entry : monitoredLists.entrySet()) {
			if (entry.getKey().equals(uuid)) {
				return new Pair(entry.getKey(), entry.getValue());
			}
		}
		return null;
	}

	@Nullable
	public <T extends IInfo> AbstractChangeableList getMonitoredList(InfoUUID uuid) {
		for (Entry<InfoUUID, AbstractChangeableList> entry : monitoredLists.entrySet()) {
			if (entry.getKey().equals(uuid)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public void tick() {

		if (updateListenerDisplays) {
			updateListenerDisplays = false;
			identityTiles.values().forEach(tile -> tile.getListenerList().getDisplayListeners().clear());

			displays.values().forEach(display -> display.container().forEachValidUUID(uuid -> {
				ILogicListenable monitor = getIdentityTile(uuid.getIdentity());
				if (monitor != null && monitor instanceof ILogicListenable) {
					monitor.getListenerList().getDisplayListeners().addListener(display, 0);
				}
			}));

		}

		if (!changedInfo.isEmpty() && !displays.isEmpty()) {
			Map<EntityPlayerMP, NBTTagList> savePackets = new HashMap<EntityPlayerMP, NBTTagList>();
			for (Entry<ILogicListenable, List<Integer>> id : changedInfo.entrySet()) {
				PL2ListenerList list = id.getKey().getListenerList();
				List<PlayerListener> listeners = list.getAllListeners(ListenerType.OLD_GUI_LISTENER, ListenerType.OLD_DISPLAY_LISTENER);
				if (!listeners.isEmpty()) {
					for (Integer i : id.getValue()) {
						InfoUUID uuid = new InfoUUID(id.getKey().getIdentity(), i);
						IInfo monitorInfo = getInfoFromUUID(uuid);
						NBTTagCompound saveTag = InfoHelper.writeInfoToNBT(new NBTTagCompound(), monitorInfo, SyncType.SAVE);
						saveTag = uuid.writeData(saveTag, SyncType.SAVE);
						PacketHelper.createInfoUpdatesForListeners(savePackets, listeners, saveTag, saveTag, false);
					}
				}
			}
			if (!savePackets.isEmpty()) {// || !syncPackets.isEmpty()) {
				savePackets.entrySet().forEach(entry -> PacketHelper.sendInfoUpdatePacket(entry.getKey(), entry.getValue(), SyncType.SAVE));
				changedInfo.clear();
			}
		}
		return;

	}

	public void changeInfo(ILogicListenable source, InfoUUID id, IInfo info) {
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
}
