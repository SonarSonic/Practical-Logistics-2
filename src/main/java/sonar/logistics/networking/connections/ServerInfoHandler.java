package sonar.logistics.networking.connections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.NBTHelper.SyncType;
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
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.types.InfoError;

public class ServerInfoHandler implements IInfoManager {

	// server side
	private int IDENTITY_COUNT; // gives unique identity to all PL2 Tiles ( which connect to networks), they can then be retrieved via their identity.
	public List<InfoUUID> changedInfo = Lists.newArrayList();
	public Map<Integer, IDisplay> displays = Maps.newLinkedHashMap();
	public boolean markDisplaysDirty = true;
	private Map<InfoUUID, IInfo> info = Maps.newLinkedHashMap();
	public Map<InfoUUID, AbstractChangeableList> monitoredLists = Maps.newLinkedHashMap();
	public Map<Integer, ILogicListenable> identityTiles = Maps.newLinkedHashMap();
	public Map<Integer, ConnectedDisplay> connectedDisplays = new ConcurrentHashMap<Integer, ConnectedDisplay>();
	// public Map<Integer, DisplayInteractionEvent> clickEvents = Maps.newHashMap();
	public Map<Integer, List<ChunkPos>> chunksToUpdate = Maps.newHashMap();

	public boolean newChunks = false;

	public int ticks;
	public boolean updateViewingMonitors;

	public void removeAll() {
		changedInfo.clear();
		displays.clear();
		info.clear();
		monitoredLists.clear();
		identityTiles.clear();
		connectedDisplays.clear();
		// clickEvents.clear();
		chunksToUpdate.clear();
		IDENTITY_COUNT=0;
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
		updateViewingMonitors = true;
	}

	public void removeIdentityTile(ILogicListenable monitor) {
		for (int i = 0; i < (monitor instanceof IInfoProvider ? ((IInfoProvider) monitor).getMaxInfo() : 1); i++) {
			info.put(new InfoUUID(monitor.getIdentity(), i), InfoError.noData);
		}
		identityTiles.remove(monitor.getIdentity());
		updateViewingMonitors = true;
	}

	public ILogicListenable getIdentityTile(int iden) {
		return identityTiles.get(iden);
	}

	public void addDisplay(IDisplay display) {
		if (!displays.containsValue(display)) {
			displays.put(display.getIdentity(), display);
			ChunkViewerHandler.instance().onDisplayAdded(display);
			updateViewingMonitors = true;
		}
	}

	public void removeDisplay(IDisplay display) {
		identityTiles.remove(display);
		ChunkViewerHandler.instance().onDisplayRemoved(display);
		updateViewingMonitors = true;
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
		if (updateViewingMonitors) {
			updateViewingMonitors = false;
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
			Map<EntityPlayerMP, NBTTagList> syncPackets = new HashMap<EntityPlayerMP, NBTTagList>();

			for (InfoUUID id : changedInfo) {
				boolean isSynced = false;
				IInfo monitorInfo = getInfoFromUUID(id);
				if (!id.valid() || monitorInfo == null)
					continue;
				NBTTagCompound updateTag = InfoHelper.writeInfoToNBT(new NBTTagCompound(), monitorInfo, SyncType.SAVE);
				NBTTagCompound saveTag = InfoHelper.writeInfoToNBT(new NBTTagCompound(), monitorInfo, SyncType.SAVE);
				boolean shouldUpdate = !updateTag.hasNoTags();
				for (IDisplay display : displays.values()) {
					if (display.container().isDisplayingUUID(id)) {
						if (shouldUpdate) {
							List<EntityPlayerMP> list = ChunkViewerHandler.instance().getWatchingPlayers(display);
							if (!list.isEmpty()) {
								updateTag = id.writeData(updateTag, SyncType.SAVE);
								PacketHelper.addInfoUpdatesToList(syncPackets, list, updateTag, saveTag, false);
							}
						}
						/* List<PlayerListener> fullViewers = list.getListeners(ListenerType.FULL_INFO); if (!fullViewers.isEmpty()) { saveTag = id.writeData(saveTag, SyncType.SAVE); PacketHelper.addInfoUpdatesToList(savePackets, fullViewers, updateTag, saveTag, true); fullViewers.forEach(viewer -> { display.getListenerList().removeListener(viewer, true, ListenerType.FULL_INFO); display.getListenerList().addListener(viewer, ListenerType.INFO); }); } */

					}
				}
			}
			if (!savePackets.isEmpty() || !syncPackets.isEmpty()) {
				savePackets.entrySet().forEach(entry -> PacketHelper.sendInfoUpdatePacket(entry.getKey(), entry.getValue(), SyncType.SAVE));
				syncPackets.entrySet().forEach(entry -> PacketHelper.sendInfoUpdatePacket(entry.getKey(), entry.getValue(), SyncType.SAVE));
				changedInfo.clear();
			}
		}
		return;

	}

	public void changeInfo(InfoUUID id, IInfo info) {
		IInfo last = getInfoFromUUID(id);
		if (info == null && last != null) {
			info = InfoError.noData;
			return;
		}
		if (info != null && (last == null || !last.isMatchingType(info) || !last.isMatchingInfo(info) || !last.isIdenticalInfo(info))) {
			setInfo(id, info);
			info.onInfoStored();
			if (!changedInfo.contains(id))
				this.changedInfo.add(id);
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
