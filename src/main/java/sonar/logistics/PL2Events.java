package sonar.logistics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.utils.SimpleProfiler;
import sonar.logistics.common.multiparts.ArrayPart;
import sonar.logistics.managers.WirelessManager;
import sonar.logistics.managers.LockedDisplayData;

public class PL2Events {

	public static final int saveDimension = 0;

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END) {
			PL2.getNetworkManager().tick();
			PL2.getServerManager().onServerTick();
			WirelessManager.tick();
			PL2.getDisplayManager().tick();
			ArrayPart.entityChanged = false;
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == saveDimension) {
			LockedDisplayData data = (LockedDisplayData) event.getWorld().getPerWorldStorage().getOrLoadData(LockedDisplayData.class, LockedDisplayData.tag);
		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == saveDimension) {
			MapStorage storage = event.getWorld().getPerWorldStorage();
			LockedDisplayData data = (LockedDisplayData) storage.getOrLoadData(LockedDisplayData.class, LockedDisplayData.tag);
			if (data == null && !PL2.getDisplayManager().lockedIDs.isEmpty()) {
				storage.setData(LockedDisplayData.tag, new LockedDisplayData(LockedDisplayData.tag));
			}
		}
	}

	@SubscribeEvent
	public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		PL2.getServerManager().sendFullPacket(event.player);
		PL2.getServerManager().requireUpdates.add((EntityPlayer) event.player);
	}

	@SubscribeEvent
	public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		PL2.getServerManager().sendFullPacket(event.player);
		PL2.getServerManager().requireUpdates.add((EntityPlayer) event.player);
	}

	@SubscribeEvent
	public void onLoggedIn(EntityEvent.EnteringChunk event) {
		if (event.getEntity() != null && !event.getEntity().getEntityWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
			PL2.getServerManager().requireUpdates.add((EntityPlayer) event.getEntity());
		}
	}

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent event) {
		ArrayPart.entityChanged = true;
	}
}