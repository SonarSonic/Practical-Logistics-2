package sonar.logistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.api.PL2API;
import sonar.logistics.commands.CommandResetInfoRegistry;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.integration.MineTweakerIntegration;
import sonar.logistics.logic.comparators.ComparatorRegistry;
import sonar.logistics.managers.CableManager;
import sonar.logistics.managers.ClientInfoManager;
import sonar.logistics.managers.DisplayManager;
import sonar.logistics.managers.NetworkManager;
import sonar.logistics.managers.ServerInfoManager;
import sonar.logistics.managers.WirelessManager;
import sonar.logistics.utils.SapphireOreGen;

@Mod(modid = PL2Constants.MODID, name = PL2Constants.NAME, 
	dependencies = 	
			"required-after:sonarcore@[" + PL2Constants.SONAR_CORE + ",);" + 
			"required-after:mcmultipart@[" + PL2Constants.MCMULTIPART + ",);",
	version = PL2Constants.VERSION)
public class PL2 {

	@SidedProxy(clientSide = "sonar.logistics.PL2Client", serverSide = "sonar.logistics.PL2Common")
	public static PL2Common proxy;

	public static SimpleNetworkWrapper network;
	public static Logger logger = (Logger) LogManager.getLogger(PL2Constants.MODID);

	@Instance(PL2Constants.MODID)
	public static PL2 instance;

	public NetworkManager networkManager = new NetworkManager();
	public CableManager cableManager = new CableManager();
	public DisplayManager displayManager = new DisplayManager();
	public ServerInfoManager serverManager = new ServerInfoManager();
	public ClientInfoManager clientManager = new ClientInfoManager();
	public ComparatorRegistry comparatorRegistry = new ComparatorRegistry();

	public static CreativeTabs creativeTab = new CreativeTabs(PL2Constants.NAME) {
		@Override
		public Item getTabIconItem() {
			return PL2Items.cable;
		}
	};

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger.info("Releasing the Kraken");
		if (!(Loader.isModLoaded("SonarCore") || Loader.isModLoaded("sonarcore"))) {
			logger.fatal("Sonar Core is not loaded");
		} else {
			logger.info("Successfully loaded with Sonar Core");
		}

		PL2API.init();
		logger.info("Initilised API");

		network = NetworkRegistry.INSTANCE.newSimpleChannel(PL2Constants.MODID);
		logger.info("Registered Network");

		PL2Common.registerPackets();
		logger.info("Registered Packets");

		PL2Config.initConfiguration(event);
		logger.info("Loaded Configuration");

		PL2Blocks.registerBlocks();
		logger.info("Loaded Blocks");

		PL2Items.registerItems();
		logger.info("Loaded Items");

		PL2Items.registerMultiparts();
		logger.info("Loaded Multiparts");

		if (PL2Config.sapphireOre) {
			GameRegistry.registerWorldGenerator(new SapphireOreGen(), 1);
			logger.info("Registered Sapphire World Generator");
		} else
			logger.info("Sapphire Ore Generation is disabled in the config");

		PL2ASMLoader.init(event);
		LogicInfoRegistry.INSTANCE.init();
		comparatorRegistry.register();
		proxy.preInit(event);
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		logger.info("Breaking into the pentagon");
		PL2Crafting.addRecipes();
		logger.info("Registered Crafting Recipes");

		OreDictionary.registerOre("oreSapphire", PL2Blocks.sapphire_ore);
		OreDictionary.registerOre("gemSapphire", PL2Items.sapphire);
		OreDictionary.registerOre("dustSapphire", PL2Items.sapphire_dust);
		logger.info("Registered OreDict");

		MinecraftForge.EVENT_BUS.register(new PL2Events());
		logger.info("Registered Events");
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new PL2Common());
		logger.info("Registered GUI Handler");
		proxy.load(event);
	}

	@EventHandler
	public void postLoad(FMLPostInitializationEvent evt) {
		logger.info("Please Wait: We are saving Harambe with a time machine");
		if (Loader.isModLoaded("MineTweaker3") || Loader.isModLoaded("MineTweaker3".toLowerCase())) {
			MineTweakerIntegration.init();
			logger.info("'Mine Tweaker' integration was loaded");
		}
		proxy.postLoad(evt);
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandResetInfoRegistry());
	}

	@EventHandler
	public void serverLoad(FMLServerStartedEvent event) {

	}

	@EventHandler
	public void serverClose(FMLServerStoppingEvent event) {
		WirelessManager.removeAll();
		getNetworkManager().removeAll();
		getCableManager().removeAll();
		getDisplayManager().removeAll();
		getClientManager().removeAll();
		getServerManager().removeAll();
		getComparatorRegistry().removeAll();
	}

	public static NetworkManager getNetworkManager() {
		return PL2.instance.networkManager;
	}

	public static CableManager getCableManager() {
		return PL2.instance.cableManager;
	}

	public static DisplayManager getDisplayManager() {
		return PL2.instance.displayManager;
	}

	public static ServerInfoManager getServerManager() {
		return PL2.instance.serverManager;
	}

	public static ClientInfoManager getClientManager() {
		return PL2.instance.clientManager;
	}

	public static IInfoManager getInfoManager(boolean isRemote) {
		return !isRemote ? getServerManager() : getClientManager();
	}

	public static ComparatorRegistry getComparatorRegistry() {
		return PL2.instance.comparatorRegistry;
	}
}