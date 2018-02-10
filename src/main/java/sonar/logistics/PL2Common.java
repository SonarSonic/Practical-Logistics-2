package sonar.logistics;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import sonar.logistics.client.gsi.IGSIRegistry;
import sonar.logistics.packets.PacketAddListener;
import sonar.logistics.packets.PacketChannels;
import sonar.logistics.packets.PacketClientEmitters;
import sonar.logistics.packets.PacketConnectedDisplayRemove;
import sonar.logistics.packets.PacketConnectedDisplayUpdate;
import sonar.logistics.packets.PacketDisplayTextEdit;
import sonar.logistics.packets.PacketEmitterStatement;
import sonar.logistics.packets.PacketGSIClick;
import sonar.logistics.packets.PacketInfoUpdates;
import sonar.logistics.packets.PacketInventoryReader;
import sonar.logistics.packets.PacketItemInteractionText;
import sonar.logistics.packets.PacketLocalProviderSelection;
import sonar.logistics.packets.PacketLocalProviders;
import sonar.logistics.packets.PacketMonitoredList;
import sonar.logistics.packets.PacketNodeFilter;
import sonar.logistics.packets.PacketWirelessStorage;

public class PL2Common {

	public static void registerPackets() {
		PL2.network.registerMessage(PacketMonitoredList.Handler.class, PacketMonitoredList.class, 0, Side.CLIENT);
		PL2.network.registerMessage(PacketChannels.Handler.class, PacketChannels.class, 1, Side.CLIENT);
		PL2.network.registerMessage(PacketAddListener.Handler.class, PacketAddListener.class, 2, Side.SERVER);
		PL2.network.registerMessage(PacketInfoUpdates.Handler.class, PacketInfoUpdates.class, 3, Side.CLIENT);
		PL2.network.registerMessage(PacketInventoryReader.Handler.class, PacketInventoryReader.class, 4, Side.SERVER);
		PL2.network.registerMessage(PacketClientEmitters.Handler.class, PacketClientEmitters.class, 5, Side.CLIENT);
		PL2.network.registerMessage(PacketLocalProviders.Handler.class, PacketLocalProviders.class, 6, Side.CLIENT);
		PL2.network.registerMessage(PacketConnectedDisplayUpdate.Handler.class, PacketConnectedDisplayUpdate.class, 7, Side.CLIENT);
		PL2.network.registerMessage(PacketGSIClick.Handler.class, PacketGSIClick.class, 8, Side.SERVER);
		//PL2.network.registerMessage(PacketClickEventClient.Handler.class, PacketClickEventClient.class, 9, Side.CLIENT);
		PL2.network.registerMessage(PacketNodeFilter.Handler.class, PacketNodeFilter.class, 10, Side.SERVER);
		PL2.network.registerMessage(PacketEmitterStatement.Handler.class, PacketEmitterStatement.class, 11, Side.SERVER);
		PL2.network.registerMessage(PacketWirelessStorage.Handler.class, PacketWirelessStorage.class, 12, Side.SERVER);
		PL2.network.registerMessage(PacketItemInteractionText.Handler.class, PacketItemInteractionText.class, 13, Side.CLIENT);
		PL2.network.registerMessage(PacketConnectedDisplayRemove.Handler.class, PacketConnectedDisplayRemove.class, 14, Side.CLIENT);
		PL2.network.registerMessage(PacketLocalProviderSelection.Handler.class, PacketLocalProviderSelection.class, 15, Side.SERVER);
		PL2.network.registerMessage(PacketDisplayTextEdit.Handler.class, PacketDisplayTextEdit.class, 16, Side.SERVER);
	}

	public IGSIRegistry getGSIRegistry(){
		return null;
	}
	
	public boolean isUsingOperator() {
		return false;
	}

	public void setUsingOperator(boolean bool) {}

	public void preInit(FMLPreInitializationEvent event) {}

	public void load(FMLInitializationEvent event) {}

	public void postLoad(FMLPostInitializationEvent evt) {}

}
