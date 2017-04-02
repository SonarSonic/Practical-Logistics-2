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
import sonar.logistics.common.multiparts.ArrayPart;
import sonar.logistics.connections.managers.EmitterManager;
import sonar.logistics.connections.managers.LockedDisplayData;

public class LogisticsEvents {

	public static final int saveDimension = 0;

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.CLIENT) {
			return;
		}
		if (event.phase == Phase.END) {
			Logistics.getNetworkManager().tick();
			Logistics.getServerManager().onServerTick();
			Logistics.getNetworkManager().updateEmitters = false;
			EmitterManager.tick(); // this must happen at the end, since the dirty boolean will be changed and will upset tiles
			Logistics.getDisplayManager().tick();
			ArrayPart.entityChanged=false;
		}
	}
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isRemote) {
			return;
		}
		if (event.getWorld().provider.getDimension() == saveDimension) {
			LockedDisplayData data = (LockedDisplayData) event.getWorld().getPerWorldStorage().getOrLoadData(LockedDisplayData.class, LockedDisplayData.tag);
		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		if (event.getWorld().isRemote) {
			return;
		}
		if (event.getWorld().provider.getDimension() == saveDimension) {
			MapStorage storage = event.getWorld().getPerWorldStorage();
			LockedDisplayData data = (LockedDisplayData) storage.getOrLoadData(LockedDisplayData.class, LockedDisplayData.tag);
			if (data == null && !Logistics.getDisplayManager().lockedIDs.isEmpty()) {
				storage.setData(LockedDisplayData.tag, new LockedDisplayData(LockedDisplayData.tag));
			}
		}
	}
	@SubscribeEvent
	public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		Logistics.getServerManager().sendFullPacket(event.player);
		Logistics.getServerManager().requireUpdates.add((EntityPlayer) event.player);
	}

	@SubscribeEvent
	public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Logistics.getServerManager().sendFullPacket(event.player);
		Logistics.getServerManager().requireUpdates.add((EntityPlayer) event.player);
	}

	@SubscribeEvent
	public void onLoggedIn(EntityEvent.EnteringChunk event) {
		if (event.getEntity() != null && !event.getEntity().getEntityWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
			Logistics.getServerManager().requireUpdates.add((EntityPlayer) event.getEntity());
		}
	}

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent event) {
		ArrayPart.entityChanged=true;
	}
}