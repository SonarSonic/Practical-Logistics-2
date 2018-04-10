package sonar.logistics.networking.displays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.core.SonarCore;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.ChunkHelper;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.ListHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.displays.DisplayGSI;

/** caches display viewers, via accessing chunk PlayerMap */
public class ChunkViewerHandler {

	public Map<DisplayGSI, List<ChunkPos>> displayChunks = new HashMap<>();
	public Map<Integer, List<EntityPlayerMP>> cachedPlayers = new HashMap<>(); // with the identity of the DisplayGSI, and current players attached to it
	public static int CHECK_RADIUS = 32;
	public Map<Integer, List<ChunkPos>> CHUNK_CHANGES = new HashMap<>();
	public boolean DISPLAY_VIEWERS_CHANGED = true;

	public static final ChunkViewerHandler instance() {
		return PL2.proxy.chunkViewer;
	}

	public void removeAll() {
		displayChunks.clear();
		cachedPlayers.clear();
		CHUNK_CHANGES.clear();
		DISPLAY_VIEWERS_CHANGED = true;
	}

	public boolean hasViewersChanged() {
		return DISPLAY_VIEWERS_CHANGED;
	}

	public void onDisplayAdded(DisplayGSI display) {
		// DISPLAY_VIEWERS_CHANGED = true;
		BlockCoords coords = display.getDisplay().getCoords();
		if (coords != null) {
			List<ChunkPos> pos = CHUNK_CHANGES.computeIfAbsent(coords.getDimension(), FunctionHelper.ARRAY);
			pos.add(new ChunkPos(coords.getBlockPos()));
		}
	}

	public void onDisplayRemoved(DisplayGSI display) {
		displayChunks.remove(display);
		cachedPlayers.remove(display);
		DISPLAY_VIEWERS_CHANGED = true;
	}

	@SubscribeEvent
	public void onChunkWatched(ChunkWatchEvent.Watch event) {
		List<ChunkPos> pos = CHUNK_CHANGES.computeIfAbsent(event.getPlayer().getEntityWorld().provider.getDimension(), FunctionHelper.ARRAY);
		pos.add(event.getChunk());
	}

	@SubscribeEvent
	public void onChunkUnwatched(ChunkWatchEvent.UnWatch event) {
		List<ChunkPos> pos = CHUNK_CHANGES.computeIfAbsent(event.getPlayer().getEntityWorld().provider.getDimension(), FunctionHelper.ARRAY);
		pos.add(event.getChunk());
	}

	public void tick() {
		if (!CHUNK_CHANGES.isEmpty()) {			
			cachedPlayers.clear();
			DISPLAY_VIEWERS_CHANGED = true;
			for(Entry<Integer, List<ChunkPos>> changes : CHUNK_CHANGES.entrySet()){
				List<DisplayGSI> displays = new ArrayList<>();
				for(ChunkPos pos : changes.getValue()){
					ListHelper.addWithCheck(displays, getDisplaysInChunk(changes.getKey(), pos));
				}
				displays.forEach(display -> display.sendInfoContainerPacket());
			}
			CHUNK_CHANGES.clear();
			return;
		}
		if (DISPLAY_VIEWERS_CHANGED) {
			DISPLAY_VIEWERS_CHANGED = !DISPLAY_VIEWERS_CHANGED;
		}
	}

	public boolean hasViewers(World world, BlockPos pos) {
		return !ChunkHelper.getChunkPlayers(world, pos).isEmpty();
	}

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
		List<EntityPlayerMP> players = cachedPlayers.computeIfAbsent(d.getDisplayGSIIdentity(), iden -> {
			World server = SonarCore.proxy.getDimension(d.getDisplay().getCoords().getDimension());
			return ChunkHelper.getChunkPlayers(server, getWatchingChunks(d));
		});
		return players;
	}

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
		List<ChunkPos> positions = displayChunks.get(gsi);
		if (positions == null) {
			displayChunks.put(gsi, ChunkHelper.getChunksInRadius(gsi.getDisplay().getCoords().getBlockPos(), CHECK_RADIUS));
			positions = displayChunks.get(gsi);
		}
		return positions;
	}

	public List<DisplayGSI> getDisplaysInChunk(int dim, ChunkPos pos) {
		List<DisplayGSI> inChunk = new ArrayList<>();
		for(Entry<DisplayGSI, List<ChunkPos>> chunks : displayChunks.entrySet()){
			if(chunks.getValue().contains(pos)){
				inChunk.add(chunks.getKey());
			}
		}
		/*
		for (DisplayGSI display : ServerInfoHandler.instance().displays.values()) {
			BlockCoords coords = display.getDisplay().getCoords();
			if (coords.getDimension() == dim && coords.insideChunk(pos)) {
				inChunk.add(display);
			}
		}
		*/
		return inChunk;
	}
}
