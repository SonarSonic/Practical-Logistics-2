package sonar.logistics.core.tiles.displays;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.core.SonarCore;
import sonar.core.helpers.ChunkHelper;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.ListHelper;
import sonar.logistics.PL2;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;
import sonar.logistics.network.packets.PacketConnectedDisplayUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** caches display listeners, via accessing chunk PlayerMap */
public class DisplayViewerHandler {

    public static int CHECK_RADIUS = 8;
	public Map<Integer, List<ChunkPos>> displayChunks = new HashMap<>();
	public Map<Integer, List<EntityPlayerMP>> cachedPlayers = new HashMap<>(); // with the identity of the DisplayGSI, and current players attached to it
	public Map<EntityPlayerMP, Map<Integer, List<ChunkPos>>> UNWATCHED_CHUNKS = new HashMap<>();
	public Map<EntityPlayerMP, Map<Integer, List<ChunkPos>>> WATCHED_CHUNKS = new HashMap<>();
	public List<DisplayGSI> ADDED_DISPLAYS = new ArrayList<>();
	public List<DisplayGSI> REMOVED_DISPLAYS = new ArrayList<>();

	public static DisplayViewerHandler instance() {
		return PL2.proxy.chunkViewer;
	}

	public void removeAll() {
		displayChunks.clear();
		cachedPlayers.clear();
		UNWATCHED_CHUNKS.clear();
		WATCHED_CHUNKS.clear();
		ADDED_DISPLAYS.clear();
		REMOVED_DISPLAYS.clear();
	}

	public void onDisplayAdded(DisplayGSI display) {
		ADDED_DISPLAYS.add(display);
	}

	public void onDisplayRemoved(DisplayGSI display) {
		REMOVED_DISPLAYS.add(display);
	}

	@SubscribeEvent
	public void onChunkWatched(ChunkWatchEvent.Watch event) {
		EntityPlayerMP player = event.getPlayer();
		int dimensionID = event.getChunkInstance().getWorld().provider.getDimension();
		WATCHED_CHUNKS.computeIfAbsent(player, FunctionHelper.HASH_MAP);
		WATCHED_CHUNKS.get(player).computeIfAbsent(dimensionID, FunctionHelper.ARRAY);
		WATCHED_CHUNKS.get(player).get(dimensionID).add(event.getChunk());
	}

	@SubscribeEvent
	public void onChunkUnwatched(ChunkWatchEvent.UnWatch event) {
		EntityPlayerMP player = event.getPlayer();
		int dimensionID = event.getChunkInstance().getWorld().provider.getDimension();
		UNWATCHED_CHUNKS.computeIfAbsent(player, FunctionHelper.HASH_MAP);
		UNWATCHED_CHUNKS.get(player).computeIfAbsent(dimensionID, FunctionHelper.ARRAY);
		UNWATCHED_CHUNKS.get(player).get(dimensionID).add(event.getChunk());
	}

	public void onViewerAdded(DisplayGSI gsi, EntityPlayerMP player) {
		if (gsi.isValid()) {
			if (gsi.getDisplay() instanceof ConnectedDisplay) {
				ConnectedDisplay display = (ConnectedDisplay) gsi.getDisplay();
				PL2.network.sendTo(new PacketConnectedDisplayUpdate(display, display.getRegistryID()), player);
			}
			gsi.sendValidatePacket(player);
			gsi.sendConnectedInfo(player);
			PL2.logger.info("Viewer Added: " + gsi.getDisplayGSIIdentity() + " " + player);
		}
	}

	public void onViewerRemoved(DisplayGSI gsi, EntityPlayerMP player) {
		gsi.sendInvalidatePacket(player);
	}

	public void updateDisplayViewers() {
		if (!ADDED_DISPLAYS.isEmpty()) {
			for (DisplayGSI gsi : ADDED_DISPLAYS) {
				if (gsi.isValid()) {
					displayChunks.remove(gsi.getDisplayGSIIdentity());
					cachedPlayers.remove(gsi.getDisplayGSIIdentity());
					List<EntityPlayerMP> watchers = getWatchingPlayers(gsi);
					watchers.forEach(watcher -> onViewerAdded(gsi, watcher));
				}
			}
			ADDED_DISPLAYS.clear();
		}
		if (!REMOVED_DISPLAYS.isEmpty()) {
			for (DisplayGSI gsi : REMOVED_DISPLAYS) {
				List<EntityPlayerMP> watchers = getWatchingPlayers(gsi);
				watchers.forEach(watcher -> onViewerRemoved(gsi, watcher));
				displayChunks.remove(gsi.getDisplayGSIIdentity());
				cachedPlayers.remove(gsi.getDisplayGSIIdentity());
			}
			REMOVED_DISPLAYS.clear();
		}

		if (!WATCHED_CHUNKS.isEmpty()) {
			for (Entry<EntityPlayerMP, Map<Integer, List<ChunkPos>>> entry : WATCHED_CHUNKS.entrySet()) {
				List<DisplayGSI> displays = new ArrayList<>();
				for (Entry<Integer, List<ChunkPos>> chunks : entry.getValue().entrySet()) {
					for (ChunkPos pos : chunks.getValue()) {
						ListHelper.addWithCheck(displays, getDisplaysInChunk(chunks.getKey(), pos));
					}
				}
				for (DisplayGSI gsi : displays) {
					List<EntityPlayerMP> players = getWatchingPlayers(gsi);
					if (!players.contains(entry.getKey())) {
						players.add(entry.getKey());
						onViewerAdded(gsi, entry.getKey());
					}
				}

			}
			WATCHED_CHUNKS.clear();
		}

		if (!UNWATCHED_CHUNKS.isEmpty()) {
			for (Entry<EntityPlayerMP, Map<Integer, List<ChunkPos>>> entry : UNWATCHED_CHUNKS.entrySet()) {
				List<DisplayGSI> displays = new ArrayList<>();
				for (Entry<Integer, List<ChunkPos>> chunks : entry.getValue().entrySet()) {
					for (ChunkPos pos : chunks.getValue()) {
						ListHelper.addWithCheck(displays, getDisplaysInChunk(chunks.getKey(), pos));
					}
				}
				for (DisplayGSI gsi : displays) {
					List<EntityPlayerMP> players = getWatchingPlayers(gsi);
					if (players != null && players.contains(entry.getKey())) {
						players.remove(entry.getKey());
						onViewerRemoved(gsi, entry.getKey());
					}
				}

			}
			UNWATCHED_CHUNKS.clear();
		}
	}

	public boolean hasViewers(World world, BlockPos pos) {
		return !ChunkHelper.getChunkPlayers(world, pos).isEmpty();
	}

	public List<DisplayGSI> getDisplaysInChunk(int dim, ChunkPos pos) {
		List<DisplayGSI> inChunk = new ArrayList<>();
		for (Entry<Integer, List<ChunkPos>> chunks : displayChunks.entrySet()) {
			if (chunks.getValue().contains(pos)) {
				DisplayGSI gsi = ServerInfoHandler.instance().getGSIMap().get(chunks.getKey());
				if(gsi != null && gsi.getWorld().provider.getDimension() == dim) {
					inChunk.add(gsi);
				}
			}
		}
		return inChunk;
	}

	//// WATCHING PLAYERS \\\\

	public List<EntityPlayerMP> getWatchingPlayers(List<DisplayGSI> displays) {
		List<EntityPlayerMP> watchingPlayers = new ArrayList<>();
		for (DisplayGSI display : displays) {
			List<EntityPlayerMP> players = getWatchingPlayers(display);
			if (!players.isEmpty()) {
				ListHelper.addWithCheck(watchingPlayers, players);
			}
		}
		return watchingPlayers;
	}

	public List<EntityPlayerMP> getWatchingPlayers(DisplayGSI d) {
		return cachedPlayers.computeIfAbsent(d.getDisplayGSIIdentity(), iden -> {
			World server = SonarCore.proxy.getDimension(d.getDisplay().getCoords().getDimension());
			return ChunkHelper.getChunkPlayers(server, getWatchingChunks(d));
		});
	}

	//// WATCHING CHUNKS \\\\

	public Map<Integer, List<ChunkPos>> getWatchingChunks(List<DisplayGSI> displays) {
		HashMap<Integer, List<ChunkPos>> watchingChunks = new HashMap<>();
		displays.forEach(display -> {
			int dim = display.getDisplay().getCoords().getDimension();
			List<ChunkPos> displayChunks = getWatchingChunks(display);
			watchingChunks.putIfAbsent(dim, new ArrayList<>());
			List<ChunkPos> chunks = watchingChunks.get(dim);
			displayChunks.forEach(chunk -> {
				if (!chunks.contains(chunk)) {
					chunks.add(chunk);
				}
			});
		});
		return watchingChunks;
	}

	public List<ChunkPos> getWatchingChunks(DisplayGSI gsi) {
		List<ChunkPos> positions = displayChunks.get(gsi.getDisplayGSIIdentity());
		if (positions == null) {
			BlockPos pos = gsi.getDisplay().getCoords().getBlockPos();
			displayChunks.put(gsi.getDisplayGSIIdentity(), ChunkHelper.getChunksInRadius(pos, CHECK_RADIUS));
			positions = displayChunks.get(gsi.getDisplayGSIIdentity());
		}
		return positions;
	}
}
