package sonar.logistics.networking.displays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.core.SonarCore;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.ChunkHelper;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.ListHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.tiles.displays.IDisplay;

/** caches display viewers, via accessing chunk PlayerMap */
public class ChunkViewerHandler {

	private static final ChunkViewerHandler INSTANCE = new ChunkViewerHandler();
	public Map<IDisplay, List<ChunkPos>> displayChunks = Maps.newHashMap();
	public Map<Integer, List<EntityPlayerMP>> cachedPlayers = Maps.newHashMap(); // with the identity of the IDisplay, and current players attached to it
	public static int CHECK_RADIUS = 32;
	public Map<Integer, List<ChunkPos>> CHUNK_CHANGES = Maps.newHashMap();
	public boolean DISPLAY_VIEWERS_CHANGED = true;

	public static final ChunkViewerHandler instance() {
		return INSTANCE;
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

	public void onDisplayAdded(IDisplay display) {
		List<ChunkPos> pos = CHUNK_CHANGES.computeIfAbsent(display.getCoords().getDimension(), FunctionHelper.ARRAY);
		pos.add(new ChunkPos(display.getCoords().getBlockPos()));
	}

	public void onDisplayRemoved(IDisplay display) {
		displayChunks.remove(display);
		cachedPlayers.remove(display);
		List<ChunkPos> pos = CHUNK_CHANGES.computeIfAbsent(display.getCoords().getDimension(), FunctionHelper.ARRAY);
		pos.add(new ChunkPos(display.getCoords().getBlockPos()));
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
			CHUNK_CHANGES.clear();
			DISPLAY_VIEWERS_CHANGED = true;
			return;
		}
		if (DISPLAY_VIEWERS_CHANGED) {
			
			DISPLAY_VIEWERS_CHANGED = !DISPLAY_VIEWERS_CHANGED;
		}
	}

	public boolean hasViewers(World world, BlockPos pos) {
		return !ChunkHelper.getChunkPlayers(world, pos).isEmpty();
	}

	public List<EntityPlayerMP> getWatchingPlayers(List<IDisplay> displays) {
		List<EntityPlayerMP> watchingPlayers = Lists.newArrayList();
		for (IDisplay display : displays) {
			List<EntityPlayerMP> players = getWatchingPlayers(display);
			if (!players.isEmpty()) {
				ListHelper.addWithCheck(watchingPlayers, players);
			}
		}
		/* List<EntityPlayerMP> watchingPlayers = Lists.newArrayList(); Map<Integer, List<ChunkPos>> chunks = getWatchingChunks(displays); chunks.forEach((DIM, positions) -> { World server = SonarCore.proxy.getDimension(DIM); List<EntityPlayerMP> chunkPlayers = ChunkHelper.getChunkPlayers(server, positions); chunkPlayers.forEach(player -> { if (!watchingPlayers.contains(player)) { watchingPlayers.add(player); } }); }); */
		return watchingPlayers;
	}

	public List<EntityPlayerMP> getWatchingPlayers(IDisplay d) {
		List<EntityPlayerMP> players = cachedPlayers.computeIfAbsent(d.getInfoContainerID(), iden -> {
			World server = SonarCore.proxy.getDimension(d.getCoords().getDimension());
			// Preconditions.checkArgument(!server.isRemote);
			return ChunkHelper.getChunkPlayers(server, getWatchingChunks(d));
		});
		return players;
	}

	public Map<Integer, List<ChunkPos>> getWatchingChunks(List<IDisplay> displays) {
		HashMap<Integer, List<ChunkPos>> watchingChunks = Maps.newHashMap();
		displays.forEach(display -> {
			int dim = display.getCoords().getDimension();
			List<ChunkPos> displayChunks = getWatchingChunks(display);
			watchingChunks.putIfAbsent(dim, Lists.newArrayList());
			List<ChunkPos> chunks = watchingChunks.get(dim);
			displayChunks.forEach(chunk -> {
				if (!chunks.contains(chunk)) {
					chunks.add(chunk);
				}
			});
		});
		return watchingChunks;
	}

	public List<ChunkPos> getWatchingChunks(IDisplay display) {
		List<ChunkPos> positions = displayChunks.get(display);
		if (positions == null) {
			displayChunks.put(display, ChunkHelper.getChunksInRadius(display.getCoords().getBlockPos(), CHECK_RADIUS));
			positions = displayChunks.get(display);
		}
		return positions;
	}

	public List<IDisplay> getDisplaysInChunk(int dim, ChunkPos pos) {
		List<IDisplay> inChunk = Lists.newArrayList();
		for (IDisplay display : PL2.getServerManager().displays.values()) {
			BlockCoords coords = display.getCoords();
			if (coords.getDimension() == dim && coords.insideChunk(pos)) {
				inChunk.add(display);
			}
		}
		return inChunk;
	}
}
