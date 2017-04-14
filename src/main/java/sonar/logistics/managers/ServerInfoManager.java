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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import sonar.core.SonarCore;
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
import sonar.logistics.api.tiles.readers.ClientViewable;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.AbstractDisplayPart;
import sonar.logistics.common.multiparts.LogisticsPart;
import sonar.logistics.common.multiparts.displays.LargeDisplayScreenPart;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.network.PacketInfoList;
import sonar.logistics.network.PacketViewables;

public class ServerInfoManager implements IInfoManager {

	public final int UPDATE_RADIUS = 64;

	public int identity;

	// server side
	public List<InfoUUID> changedInfo = Lists.newArrayList();
	public List<IDisplay> displays = Lists.newArrayList();
	public boolean markDisplaysDirty = true;
	// public List<EntityPlayer> requireUpdates = Lists.newArrayList();
	// public Map<EntityPlayer, ArrayList<IDisplay>> viewables = Maps.newHashMap();

	public Map<InfoUUID, IInfo> info = Maps.newLinkedHashMap();
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

	public int getNextIdentity() {
		identity++;
		return identity;
	}

	public boolean enableEvents() {
		return !displays.isEmpty();
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
			info.remove(new InfoUUID(monitor.getIdentity(), i));
		}
		identityTiles.remove(monitor.getIdentity());
		updateViewingMonitors = true;
	}

	public ILogicListenable getIdentityTile(int iden) {
		return identityTiles.get(iden);
	}

	public void addDisplay(IDisplay display) {
		if (!displays.contains(display) && displays.add(display)) {
			addChunkFromDisplay(display);
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
		displays.forEach(d -> d.getListenerList().removeListener(player, ListenerType.INFO));
	}

	public void addChunkFromDisplay(IDisplay display) {
		BlockCoords coords = display.getCoords();
		WorldServer world = (WorldServer) coords.getWorld();
		addChangedChunk(coords.getDimension(), SonarHelper.getChunkFromPos(coords.getX(), coords.getZ()));
	}

	public void removeListenersFromDisplay(IDisplay display) {
		List<PlayerListener> listeners = display.getListenerList().getListeners(ListenerType.INFO);
		listeners.forEach(pl -> display.getListenerList().removeListener(pl, ListenerType.INFO));
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

	public List<IDisplay> getViewableDisplays(EntityPlayer player, boolean sendSyncPackets) {
		List<IDisplay> viewable = Lists.newArrayList();
		World world = player.getEntityWorld();
		PlayerChunkMap manager = ((WorldServer) world).getPlayerChunkMap();
		for (IDisplay display : displays) {
			if (manager.isPlayerWatchingChunk((EntityPlayerMP) player, display.getCoords().getX() >> 4, display.getCoords().getZ() >> 4)) {
				viewable.add(display);
			}
			if (sendSyncPackets && display instanceof LargeDisplayScreenPart) {
				LargeDisplayScreenPart part = (LargeDisplayScreenPart) display;
				SonarMultipartHelper.sendMultipartSyncToPlayer(part, (EntityPlayerMP) player);
			}
		}
		return viewable;
	}

	public void sendFullPacket(EntityPlayer player) {
		if (player != null) {
			List<IInfo> infoList = getInfoFromUUIDs(getUUIDsToSync(getViewableDisplays(player, true)));
			if (infoList.isEmpty()) {
				return;
			}
			NBTTagList packetList = new NBTTagList();
			for (IInfo info : infoList) {
				if (info != null && info.isValid() && !info.isHeader()) {
					packetList.appendTag(InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE));
				}
			}
			if (!packetList.hasNoTags()) {
				NBTTagCompound packetTag = new NBTTagCompound();
				packetTag.setTag("infoList", packetList);
				PL2.network.sendTo(new PacketInfoList(packetTag, SyncType.SAVE), (EntityPlayerMP) player);
			}
		}
	}

	public List<IInfo> getInfoFromUUIDs(List<InfoUUID> ids) {
		List<IInfo> infoList = Lists.newArrayList();
		for (InfoUUID id : ids) {
			ILogicListenable monitor = CableHelper.getMonitorFromIdentity(id.getIdentity(), false);
			if (monitor != null && monitor instanceof IInfoProvider) {
				IInfo info = ((IInfoProvider) monitor).getMonitorInfo(id.channelID);
				if (info != null) {
					infoList.add(info);
				}
			}
		}
		return infoList;
	}

	public List<InfoUUID> getUUIDsToSync(List<IDisplay> displays) {
		ArrayList<InfoUUID> ids = Lists.newArrayList();
		for (IDisplay display : displays) {
			IInfoContainer container = display.container();
			for (int i = 0; i < container.getMaxCapacity(); i++) {
				InfoUUID id = container.getInfoUUID(i);
				if (id != null && id.valid() && !ids.contains(id)) {
					ids.add(id);
				}
			}
		}
		return ids;
	}

	public IInfo getInfoFromUUID(InfoUUID uuid) {
		return info.get(uuid);
	}

	public TargetPoint getTargetPointFromPlayer(EntityPlayer player) {
		return new TargetPoint(player.getEntityWorld().provider.getDimension(), player.posX, player.posY, player.posZ, UPDATE_RADIUS);
	}

	// client methods
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
		newChunks = false;
	}

	public void onServerTick() {
		if (newChunks) {
			updateChunks();
		}
		if (updateViewingMonitors) {
			updateViewingMonitors = false;
			for (ILogicListenable monitor : identityTiles.values()) {
				((ILogicListenable) monitor).getListenerList().clearSubLists(true);
			}
			for (IDisplay display : displays) {
				for (int i = 0; i < display.container().getMaxCapacity(); i++) {
					InfoUUID uuid = display.container().getInfoUUID(i);
					if (uuid != null) {
						ILogicListenable monitor = CableHelper.getMonitorFromIdentity(uuid.getIdentity(), false);
						if (monitor != null && monitor instanceof ILogicListenable) {
							monitor.getListenerList().addSubListenable(display);
						}
					}
				}
			}
		}

		if (!changedInfo.isEmpty() && !displays.isEmpty()) {
			Map<PlayerListener, NBTTagList> savePackets = new HashMap<PlayerListener, NBTTagList>();
			Map<PlayerListener, NBTTagList> syncPackets = new HashMap<PlayerListener, NBTTagList>();

			for (InfoUUID id : changedInfo) {
				boolean isSynced = false;
				IInfo monitorInfo = info.get(id);
				if (id.valid() && monitorInfo != null) {

					NBTTagCompound updateTag = InfoHelper.writeInfoToNBT(new NBTTagCompound(), monitorInfo, SyncType.SAVE);
					NBTTagCompound saveTag = InfoHelper.writeInfoToNBT(new NBTTagCompound(), monitorInfo, SyncType.SAVE);
					boolean shouldUpdate = !updateTag.hasNoTags();

					for (IDisplay display : displays) {
						if (display.container().monitorsUUID(id)) {
							ListenerList<PlayerListener> list = display.getListenerList();
							if (shouldUpdate) {
								List<PlayerListener> listeners = list.getListeners(ListenerType.INFO);
								updateTag = id.writeData(updateTag, SyncType.SAVE);
								addPacketsToList(syncPackets, listeners, updateTag, saveTag, false);
							}
							List<PlayerListener> fullViewers = list.getListeners(ListenerType.FULL_INFO);
							if (!fullViewers.isEmpty()) {
								saveTag = id.writeData(saveTag, SyncType.SAVE);
								addPacketsToList(savePackets, fullViewers, updateTag, saveTag, true);
								fullViewers.forEach(viewer -> {
									display.getListenerList().removeListener(viewer, ListenerType.FULL_INFO);
									display.getListenerList().addListener(viewer, ListenerType.INFO);
								});
							}
						}
					}
				}
			}

			if (!savePackets.isEmpty()) {
				savePackets.entrySet().forEach(entry -> sendPlayerPacket(entry.getKey(), entry.getValue(), SyncType.SAVE));
			}
			if (!syncPackets.isEmpty()) {
				syncPackets.entrySet().forEach(entry -> sendPlayerPacket(entry.getKey(), entry.getValue(), SyncType.SAVE));
			}
			if (!savePackets.isEmpty() || !syncPackets.isEmpty())
				changedInfo.clear();
		}
		return;

	}

	public void addPacketsToList(Map<PlayerListener, NBTTagList> listenerPackets, List<PlayerListener> listeners, NBTTagCompound updateTag, NBTTagCompound saveTag, boolean fullPacket) {
		for (PlayerListener listener : listeners) {
			NBTTagList list = listenerPackets.get(listener);
			if (list == null) {
				listenerPackets.put(listener, new NBTTagList());
				list = listenerPackets.get(listener);
			}
			list.appendTag(fullPacket ? saveTag.copy() : updateTag.copy());
		}
	}

	public void sendPlayerPacket(PlayerListener listener, NBTTagList list, SyncType type) {
		if (list.hasNoTags()) {
			return;
		}
		NBTTagCompound packetTag = new NBTTagCompound();
		packetTag.setTag("infoList", list);
		PL2.network.sendTo(new PacketInfoList(packetTag, type), listener.player);
	}

	public void changeInfo(InfoUUID id, IInfo info) {
		IInfo last = this.info.get(id);
		if (info == null && last != null) {
			info = InfoError.noData;
		}
		if (info != null && (last == null || !last.isMatchingType(info) || !last.isMatchingInfo(info) || !last.isIdenticalInfo(info))) {
			this.info.put(id, info);
			this.changedInfo.add(id);
		}
	}

	public List<ILogicListenable> getViewables(List<ILogicListenable> viewables, AbstractDisplayPart part) {
		ILogisticsNetwork networkCache = part.getNetwork();
		ISlottedPart connectedPart = part.getContainer().getPartInSlot(PartSlot.getFaceSlot(part.face));
		if (connectedPart != null && connectedPart instanceof IInfoProvider) {
			if (!viewables.contains((IInfoProvider) connectedPart))
				viewables.add((IInfoProvider) connectedPart);
		} else {
			for (IInfoProvider monitor : networkCache.getLocalInfoProviders()) {
				if (!viewables.contains(monitor))
					viewables.add(monitor);
			}
		}
		return viewables;
	}

	public void sendViewablesToClientFromScreen(AbstractDisplayPart part, EntityPlayer player) {
		List<ILogicListenable> viewables = new ArrayList<ILogicListenable>();
		int identity = part.getIdentity();
		if (part instanceof ILargeDisplay) {
			ConnectedDisplay screen = ((ILargeDisplay) part).getDisplayScreen();
			if (screen != null && screen.getTopLeftScreen() != null) {
				identity = ((AbstractDisplayPart) screen.getTopLeftScreen()).getIdentity();
			}
			viewables = screen != null ? screen.getLogicMonitors(viewables) : getViewables(viewables, part);
		} else {
			viewables = getViewables(viewables, part);
		}

		List<ClientViewable> clientMonitors = Lists.newArrayList();
		viewables.forEach(viewable -> {
			viewable.getListenerList().addListener(player, ListenerType.TEMPORARY);
			clientMonitors.add(new ClientViewable(viewable));
		});
		PL2.network.sendTo(new PacketViewables(clientMonitors, identity), (EntityPlayerMP) player);
	}

	public void sendViewablesToClient(LogisticsPart part, int identity, EntityPlayer player) {
		List<IInfoProvider> viewables = part.getNetwork().getLocalInfoProviders();
		List<ClientViewable> clientMonitors = Lists.newArrayList();
		viewables.forEach(viewable -> {
			viewable.getListenerList().addListener(player, ListenerType.TEMPORARY);
			clientMonitors.add(new ClientViewable(viewable));
		});
		PL2.network.sendTo(new PacketViewables(clientMonitors, identity), (EntityPlayerMP) player);
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
}
