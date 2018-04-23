package sonar.logistics;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.networking.ClientInfoHandler;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.cabling.CableConnectionHandler;
import sonar.logistics.networking.cabling.RedstoneConnectionHandler;
import sonar.logistics.networking.cabling.WirelessDataManager;
import sonar.logistics.networking.cabling.WirelessRedstoneManager;
import sonar.logistics.networking.displays.ChunkViewerHandler;
import sonar.logistics.networking.displays.DisplayHandler;
import sonar.logistics.networking.events.LogisticsEventHandler;
import sonar.logistics.packets.PacketAddListener;
import sonar.logistics.packets.PacketChannels;
import sonar.logistics.packets.PacketClientEmitters;
import sonar.logistics.packets.PacketConnectedDisplayRemove;
import sonar.logistics.packets.PacketConnectedDisplayUpdate;
import sonar.logistics.packets.PacketDisplayTextEdit;
import sonar.logistics.packets.PacketEmitterStatement;
import sonar.logistics.packets.PacketInfoUpdates;
import sonar.logistics.packets.PacketInventoryReader;
import sonar.logistics.packets.PacketItemInteractionText;
import sonar.logistics.packets.PacketLocalProviderSelection;
import sonar.logistics.packets.PacketLocalProviders;
import sonar.logistics.packets.PacketMonitoredList;
import sonar.logistics.packets.PacketNodeFilter;
import sonar.logistics.packets.PacketWirelessStorage;
import sonar.logistics.packets.gsi.PacketGSIConnectedDisplayValidate;
import sonar.logistics.packets.gsi.PacketGSIContentsPacket;
import sonar.logistics.packets.gsi.PacketGSIClick;
import sonar.logistics.packets.gsi.PacketGSIElement;
import sonar.logistics.packets.gsi.PacketGSIInvalidate;
import sonar.logistics.packets.gsi.PacketGSIStandardDisplayValidate;

public class PL2Common {

	public ServerInfoHandler server_info_manager;
	public LogisticsNetworkHandler networkManager;
	public WirelessDataManager wirelessDataManager;
	public WirelessRedstoneManager wirelessRedstoneManager;
	public CableConnectionHandler cableManager;
	public RedstoneConnectionHandler redstoneManager;
	public DisplayHandler server_display_manager;
	public ChunkViewerHandler chunkViewer;
	public LogisticsEventHandler eventHandler;
	
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
		//make a server to client version?
		PL2.network.registerMessage(PacketGSIElement.Handler.class, PacketGSIElement.class, 9, Side.SERVER);
		//
		PL2.network.registerMessage(PacketNodeFilter.Handler.class, PacketNodeFilter.class, 10, Side.SERVER);
		PL2.network.registerMessage(PacketEmitterStatement.Handler.class, PacketEmitterStatement.class, 11, Side.SERVER);
		PL2.network.registerMessage(PacketWirelessStorage.Handler.class, PacketWirelessStorage.class, 12, Side.SERVER);
		PL2.network.registerMessage(PacketItemInteractionText.Handler.class, PacketItemInteractionText.class, 13, Side.CLIENT);
		PL2.network.registerMessage(PacketConnectedDisplayRemove.Handler.class, PacketConnectedDisplayRemove.class, 14, Side.CLIENT);
		PL2.network.registerMessage(PacketLocalProviderSelection.Handler.class, PacketLocalProviderSelection.class, 15, Side.SERVER);
		PL2.network.registerMessage(PacketDisplayTextEdit.Handler.class, PacketDisplayTextEdit.class, 16, Side.SERVER);
		PL2.network.registerMessage(PacketGSIContentsPacket.Handler.class, PacketGSIContentsPacket.class, 17, Side.CLIENT);
		PL2.network.registerMessage(PacketGSIConnectedDisplayValidate.Handler.class, PacketGSIConnectedDisplayValidate.class, 18, Side.CLIENT);
		PL2.network.registerMessage(PacketGSIStandardDisplayValidate.Handler.class, PacketGSIStandardDisplayValidate.class, 19, Side.CLIENT);
		PL2.network.registerMessage(PacketGSIInvalidate.Handler.class, PacketGSIInvalidate.class, 20, Side.CLIENT);
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
		
		chunkViewer = new ChunkViewerHandler();
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
