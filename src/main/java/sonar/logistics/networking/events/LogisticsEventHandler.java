package sonar.logistics.networking.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.IRedstoneCable;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.cabling.CableConnectionHandler;
import sonar.logistics.networking.cabling.RedstoneConnectionHandler;

public class LogisticsEventHandler {

	public static void registerHandlers() {
		MinecraftForge.EVENT_BUS.register(LogisticsEventHandler.class);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPartAdded(NetworkPartEvent.AddedPart event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof ILogicListenable){
				ServerInfoHandler.instance().addIdentityTile((ILogicListenable) event.tile);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPartRemoved(NetworkPartEvent.RemovedPart event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof ILogicListenable){
				ServerInfoHandler.instance().removeIdentityTile((ILogicListenable) event.tile);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onCableAdded(NetworkCableEvent.AddedCable event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof IDataCable)
				CableConnectionHandler.instance().queueCableAddition((IDataCable) event.tile);
			if (event.tile instanceof IRedstoneCable)
				RedstoneConnectionHandler.instance().queueCableAddition((IRedstoneCable) event.tile);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onCableRemoved(NetworkCableEvent.RemovedCable event) {
		if (!event.world.isRemote) {
			if (event.tile instanceof IDataCable)
				CableConnectionHandler.instance().queueCableRemoval((IDataCable) event.tile);
			if (event.tile instanceof IRedstoneCable)
				RedstoneConnectionHandler.instance().queueCableRemoval((IRedstoneCable) event.tile);
		}

	}

}
