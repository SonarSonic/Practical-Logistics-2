package sonar.logistics;

import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import sonar.logistics.common.multiparts2.cables.CableConnectionHandler;
import sonar.logistics.common.multiparts2.nodes.TileArray;
import sonar.logistics.managers.WirelessManager;
import sonar.logistics.worlddata.IdentityCountData;
import sonar.logistics.worlddata.LockedDisplayData;

public class PL2Events {

	public static final int saveDimension = 0;

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END) {
			CableConnectionHandler.tick();
			
			PL2.getNetworkManager().tick();
			PL2.getServerManager().onServerTick();
			WirelessManager.tick();
			PL2.getDisplayManager().tick();
			TileArray.entityChanged = false;
			
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == saveDimension) {
			MapStorage storage = event.getWorld().getPerWorldStorage();
			storage.getOrLoadData(LockedDisplayData.class, LockedDisplayData.tag);
			storage.getOrLoadData(IdentityCountData.class, IdentityCountData.tag);

		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == saveDimension) {
			MapStorage storage = event.getWorld().getPerWorldStorage();
			LockedDisplayData displayData = (LockedDisplayData) storage.getOrLoadData(LockedDisplayData.class, LockedDisplayData.tag);
			if (displayData == null && !PL2.getDisplayManager().lockedIDs.isEmpty()) {
				storage.setData(LockedDisplayData.tag, new LockedDisplayData(LockedDisplayData.tag));
			}

			IdentityCountData countData = (IdentityCountData) storage.getOrLoadData(IdentityCountData.class, IdentityCountData.tag);
			if (countData == null) {
				storage.setData(IdentityCountData.tag, new IdentityCountData(IdentityCountData.tag));
			}

		}
	}

	@SubscribeEvent
	public void watchChunk(ChunkWatchEvent.Watch event) {
		PL2.getServerManager().addListener(event.getChunk(), event.getPlayer());
	}

	@SubscribeEvent
	public void unWatchChunk(ChunkWatchEvent.UnWatch event) {
		PL2.getServerManager().removeListener(event.getChunk(), event.getPlayer());
	}

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent event) {
		TileArray.entityChanged = true;
	}
}