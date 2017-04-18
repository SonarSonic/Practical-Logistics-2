package sonar.logistics.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.SonarHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.listener.ListenerList;
import sonar.core.listener.PlayerListener;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.info.render.IInfoContainer;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayInteractionEvent;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.readers.ClientLocalProvider;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.AbstractDisplayPart;
import sonar.logistics.common.multiparts.LogisticsPart;
import sonar.logistics.common.multiparts.displays.LargeDisplayScreenPart;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.network.PacketInfoUpdates;
import sonar.logistics.network.PacketLocalProviders;

public class ServerInfoManager implements IInfoManager {

	// server side
	public List<InfoUUID> changedInfo = Lists.newArrayList();
	public List<IDisplay> displays = Lists.newArrayList();
	public boolean markDisplaysDirty = true;

	private Map<InfoUUID, IInfo> info = Maps.newLinkedHashMap();
	public Map<InfoUUID, MonitoredList<?>> monitoredLists = Maps.newLinkedHashMap();
	public Map<Integer, ILogicListenable> identityTiles = Maps.newLinkedHashMap();

	public Map<Integer, ConnectedDisplay> connectedDisplays = new ConcurrentHashMap<Integer, ConnectedDisplay>();

	public Map<Integer, DisplayInteractionEvent> clickEvents = Maps.newHashMap();

	public Map<Integer, List<ChunkPos>> chunksToUpdate = Maps.newHashMap();
	public boolean newChunks = false;

	public int ticks;
	public boolean updateViewingMonitors;

	public void removeAll() {
		changedInfo.clear();
		displays.clear();
		// requireUpdates.clear();
		// viewables.clear();
		monitoredLists.clear();
		identityTiles.clear();
		info.clear();
		connectedDisplays.clear();
		clickEvents.clear();
		chunksToUpdate.clear();
	}

	public boolean enableEvents() {
		return !displays.isEmpty();
	}

	public IInfo getInfoFromUUID(InfoUUID uuid) {
		return info.get(uuid);
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
		if (!displays.contains(display) && displays.add(display)) {
			addListenersFromDisplay(display);
			updateViewingMonitors = true;
		}
	}

	public void removeDisplay(IDisplay display) {
		if (displays.remove(display)) {
			removeListenersFromDisplay(display);
			updateViewingMonitors = true;
		}
	}

	public void addListener(ChunkPos chunkPos, EntityPlayerMP player) {
		List<IDisplay> displays = getDisplaysInChunk(player.dimension, chunkPos);
		displays.forEach(d -> d.getListenerList().addListener(player, ListenerType.TEMPORARY, ListenerType.FULL_INFO));
	}

	public void removeListener(ChunkPos chunkPos, EntityPlayerMP player) {
		List<IDisplay> displays = getDisplaysInChunk(player.dimension, chunkPos);
		displays.forEach(d -> d.getListenerList().removeListener(player, true, ListenerType.INFO));
	}

	public void addListenersFromDisplay(IDisplay display) {
		BlockCoords coords = display.getCoords();
		WorldServer world = (WorldServer) coords.getWorld();
		addChangedChunk(coords.getDimension(), SonarHelper.getChunkFromPos(coords.getX(), coords.getZ()));
	}

	public void removeListenersFromDisplay(IDisplay display) {
		List<PlayerListener> listeners = display.getListenerList().getListeners(ListenerType.INFO);
		listeners.forEach(pl -> display.getListenerList().removeListener(pl, true, ListenerType.INFO));
	}

	public void addChangedChunk(int dimension, ChunkPos chunkPos) {
		List<ChunkPos> chunks = chunksToUpdate.computeIfAbsent(dimension, FunctionHelper.ARRAY);
		if (!chunks.contains(chunkPos)) {
			chunks.add(chunkPos);
			newChunks = true;
		}
	}

	public List<IDisplay> getDisplaysInChunk(int dim, ChunkPos pos) {
		List<IDisplay> inChunk = Lists.newArrayList();
		for (IDisplay display : displays) {
			BlockCoords coords = display.getCoords();
			if (coords.getDimension() == dim && coords.insideChunk(pos)) {
				inChunk.add(display);
			}
		}
		return inChunk;
	}

	@Nullable
	public Pair<InfoUUID, MonitoredList<?>> getMonitorFromServer(InfoUUID uuid) {
		for (Entry<InfoUUID, ?> entry : monitoredLists.entrySet()) {
			if (entry.getKey().equals(uuid)) {
				return new Pair(entry.getKey(), entry.getValue());
			}
		}
		return null;
	}

	public <T extends IInfo> MonitoredList<T> getMonitoredList(int networkID, InfoUUID uuid) {
		MonitoredList<T> list = MonitoredList.<T>newMonitoredList(networkID);
		monitoredLists.putIfAbsent(uuid, list);
		for (Entry<InfoUUID, MonitoredList<?>> entry : monitoredLists.entrySet()) {
			if (entry.getValue().networkID == networkID && entry.getKey().equals(uuid)) {
				return (MonitoredList<T>) entry.getValue();
			}
		}
		return list;
	}

	public void updateChunks() {
		for (Entry<Integer, List<ChunkPos>> dims : chunksToUpdate.entrySet()) {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			WorldServer world = server.worldServerForDimension(dims.getKey());
			PlayerChunkMap chunkMap = world.getPlayerChunkMap();
			for (ChunkPos chunkPos : dims.getValue()) {
				PlayerChunkMapEntry entry = chunkMap.getEntry(chunkPos.chunkXPos, chunkPos.chunkZPos);
				List<EntityPlayerMP> players = SonarHelper.getPlayersWatchingChunk(entry);
				if (players.isEmpty()) {
					continue;
				}
				for (IDisplay d : getDisplaysInChunk(dims.getKey(), chunkPos)) {
					ListenerList list = d.getListenerList();
					players.forEach(p -> list.addListener(p, ListenerType.TEMPORARY, ListenerType.FULL_INFO));
				}
			}
		}
		chunksToUpdate.clear();
		newChunks = false;
	}

	public void onServerTick() {
		if (newChunks) {
			updateChunks();
		}
		if (updateViewingMonitors) {
			updateViewingMonitors = false;
			identityTiles.values().forEach(tile -> tile.getListenerList().clearSubLists(true));
			
			displays.forEach(display -> display.container().forEachValidUUID(uuid -> {
				ILogicListenable monitor = CableHelper.getMonitorFromIdentity(uuid.getIdentity(), false);
				if (monitor != null && monitor instanceof ILogicListenable) {
					monitor.getListenerList().addSubListenable(display);
				}
			}));

		}

		if (!changedInfo.isEmpty() && !displays.isEmpty()) {

			Map<PlayerListener, NBTTagList> savePackets = new HashMap<PlayerListener, NBTTagList>();
			Map<PlayerListener, NBTTagList> syncPackets = new HashMap<PlayerListener, NBTTagList>();

			for (InfoUUID id : changedInfo) {
				boolean isSynced = false;
				IInfo monitorInfo = getInfoFromUUID(id);
				if (!id.valid() || monitorInfo == null)
					continue;
				NBTTagCompound updateTag = InfoHelper.writeInfoToNBT(new NBTTagCompound(), monitorInfo, SyncType.SAVE);
				NBTTagCompound saveTag = InfoHelper.writeInfoToNBT(new NBTTagCompound(), monitorInfo, SyncType.SAVE);
				boolean shouldUpdate = !updateTag.hasNoTags();
				for (IDisplay display : displays) {
					if (display.container().isDisplayingUUID(id)) {
						ListenerList<PlayerListener> list = display.getListenerList();
						if (shouldUpdate) {
							List<PlayerListener> listeners = list.getListeners(ListenerType.INFO);
							updateTag = id.writeData(updateTag, SyncType.SAVE);
							PacketHelper.addInfoUpdatesToList(syncPackets, listeners, updateTag, saveTag, false);
						}
						List<PlayerListener> fullViewers = list.getListeners(ListenerType.FULL_INFO);
						if (!fullViewers.isEmpty()) {
							saveTag = id.writeData(saveTag, SyncType.SAVE);
							PacketHelper.addInfoUpdatesToList(savePackets, fullViewers, updateTag, saveTag, true);
							fullViewers.forEach(viewer -> {
								display.getListenerList().removeListener(viewer, true, ListenerType.FULL_INFO);
								display.getListenerList().addListener(viewer, ListenerType.INFO);
							});
						}

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
