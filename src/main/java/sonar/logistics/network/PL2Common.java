package sonar.logistics.network;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import sonar.logistics.PL2;
import sonar.logistics.api.base.IInfoManager;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.PacketChannels;
import sonar.logistics.base.data.DataFactory;
import sonar.logistics.base.events.LogisticsEventHandler;
import sonar.logistics.core.tiles.connections.data.handling.CableConnectionHandler;
import sonar.logistics.core.tiles.connections.data.network.LogisticsNetworkHandler;
import sonar.logistics.core.tiles.connections.redstone.handling.RedstoneConnectionHandler;
import sonar.logistics.core.tiles.displays.DisplayHandler;
import sonar.logistics.core.tiles.displays.DisplayViewerHandler;
import sonar.logistics.core.tiles.wireless.base.PacketClientEmitters;
import sonar.logistics.core.tiles.wireless.handling.WirelessDataManager;
import sonar.logistics.core.tiles.wireless.handling.WirelessRedstoneManager;
import sonar.logistics.network.packets.*;
import sonar.logistics.network.packets.gsi.*;

public class PL2Common {

	public ServerInfoHandler server_info_manager;
	public LogisticsNetworkHandler networkManager;
	public WirelessDataManager wirelessDataManager;
	public WirelessRedstoneManager wirelessRedstoneManager;
	public CableConnectionHandler cableManager;
	public RedstoneConnectionHandler redstoneManager;
	public DisplayHandler server_display_manager;
	public DisplayViewerHandler chunkViewer;
	public LogisticsEventHandler eventHandler;
	public DataFactory dataFactory;
	public static int PACKET_ID = 0;
	
	public static void registerPackets() {
		PACKET_ID = 0;
		PL2.network.registerMessage(PacketMonitoredList.Handler.class, PacketMonitoredList.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketChannels.Handler.class, PacketChannels.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketInfoUpdates.Handler.class, PacketInfoUpdates.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketInventoryReader.Handler.class, PacketInventoryReader.class, PACKET_ID++, Side.SERVER);
		PL2.network.registerMessage(PacketClientEmitters.Handler.class, PacketClientEmitters.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketLocalProviders.Handler.class, PacketLocalProviders.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketConnectedDisplayUpdate.Handler.class, PacketConnectedDisplayUpdate.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketGSIClick.Handler.class, PacketGSIClick.class, PACKET_ID++, Side.SERVER);
		PL2.network.registerMessage(PacketGSIElement.Handler.class, PacketGSIElement.class, PACKET_ID++, Side.SERVER);
		PL2.network.registerMessage(PacketNodeFilter.Handler.class, PacketNodeFilter.class, PACKET_ID++, Side.SERVER);
		PL2.network.registerMessage(PacketEmitterStatement.Handler.class, PacketEmitterStatement.class, PACKET_ID++, Side.SERVER);
		PL2.network.registerMessage(PacketWirelessStorage.Handler.class, PacketWirelessStorage.class, PACKET_ID++, Side.SERVER);
		PL2.network.registerMessage(PacketItemInteractionText.Handler.class, PacketItemInteractionText.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketConnectedDisplayRemove.Handler.class, PacketConnectedDisplayRemove.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketGSISavedDataPacket.Handler.class, PacketGSISavedDataPacket.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketGSIConnectedDisplayValidate.Handler.class, PacketGSIConnectedDisplayValidate.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketGSIStandardDisplayValidate.Handler.class, PacketGSIStandardDisplayValidate.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketGSIInvalidate.Handler.class, PacketGSIInvalidate.class, PACKET_ID++, Side.CLIENT);
		PL2.network.registerMessage(PacketHolographicDisplayScaling.Handler.class, PacketHolographicDisplayScaling.class, PACKET_ID++, Side.SERVER);
	}

	public ClientInfoHandler getClientManager() {
		return null;
	}

	public ServerInfoHandler getServerManager() {
		return server_info_manager;
	}

	public IInfoManager getInfoManager(boolean isRemote) {
		return server_info_manager;
	}

	public DisplayHandler getDisplayManager(boolean isRemote) {
		return server_display_manager;
	}
	
	public boolean isUsingOperator() {
		return false;
	}
	
	public void setUsingOperator(boolean bool) {}
	
	public void initHandlers(){
		server_info_manager = new ServerInfoHandler();
		PL2.logger.info("Initialised Server Info Handler");
		
		networkManager = new LogisticsNetworkHandler();
		PL2.logger.info("Initialised Network Handler");

		dataFactory = new DataFactory();
		PL2.logger.info("Initialised Data Factory");
		
		wirelessDataManager = new WirelessDataManager();
		PL2.logger.info("Initialised Wireless Data Manager");
		
		wirelessRedstoneManager = new WirelessRedstoneManager();
		PL2.logger.info("Initialised Wireless Redstone Manager");
		
		cableManager = new CableConnectionHandler();
		PL2.logger.info("Initialised Cable Connection Handler");
		
		redstoneManager = new RedstoneConnectionHandler();
		PL2.logger.info("Initialised Redstone Connection Handler");
		
		server_display_manager = new DisplayHandler();
		MinecraftForge.EVENT_BUS.register(server_display_manager);
		PL2.logger.info("Initialised Server Display Handler");
		
		chunkViewer = new DisplayViewerHandler();
		MinecraftForge.EVENT_BUS.register(chunkViewer);
		PL2.logger.info("Initialised Chunk Viewer Handler");	
		
		eventHandler = new LogisticsEventHandler();
		MinecraftForge.EVENT_BUS.register(eventHandler);
		PL2.logger.info("Initialised Event Handler");		
	}
	
	public void removeAll(){
		server_info_manager.removeAll();	
		PL2.logger.info("Cleared Server Info Handler");	
		
		networkManager.removeAll();
		PL2.logger.info("Cleared Network Handler");

		networkManager.removeAll();
		PL2.logger.info("Cleared Data Factory");
		
		wirelessDataManager.removeAll();
		PL2.logger.info("Cleared Wireless Data Manager");
		
		wirelessRedstoneManager.removeAll();
		PL2.logger.info("Cleared Wireless Redstone Manager");
		
		cableManager.removeAll();
		PL2.logger.info("Cleared Cable Connection Handler");
		
		redstoneManager.removeAll();
		PL2.logger.info("Cleared Redstone Connection Handler");
		
		server_display_manager.removeAll();
		PL2.logger.info("Cleared Server Display Handler");
		
		chunkViewer.removeAll();
		PL2.logger.info("Cleared Chunk Viewer Handler");
	}

	public void preInit(FMLPreInitializationEvent event) {
		initHandlers();
	}

	public void load(FMLInitializationEvent event) {}

	public void postLoad(FMLPostInitializationEvent evt) {}

}
