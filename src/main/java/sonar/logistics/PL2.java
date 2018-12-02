package sonar.logistics;

import mcmultipart.api.slot.IPartSlot;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sonar.logistics.base.events.PL2Events;
import sonar.logistics.base.utils.commands.CommandResetInfoRegistry;
import sonar.logistics.base.utils.slots.EnumDisplayFaceSlot;
import sonar.logistics.base.utils.worlddata.ConnectedDisplayData;
import sonar.logistics.base.utils.worlddata.GSIData;
import sonar.logistics.base.utils.worlddata.IdentityCountData;
import sonar.logistics.base.utils.worldgen.SapphireOreGen;
import sonar.logistics.core.tiles.displays.info.MasterInfoRegistry;
import sonar.logistics.core.tiles.displays.tiles.holographic.EntityHolographicDisplay;
import sonar.logistics.network.PL2Common;

@Mod.EventBusSubscriber
@Mod(modid = PL2Constants.MODID, name = PL2Constants.NAME, dependencies = PL2Constants.DEPENDENCIES, version = PL2Constants.VERSION)
public class PL2 {

	@SidedProxy(clientSide = "sonar.logistics.network.PL2Client", serverSide = "sonar.logistics.network.PL2Common")
	public static PL2Common proxy;

	public static SimpleNetworkWrapper network;
	public static Logger logger = (Logger) LogManager.getLogger(PL2Constants.MODID);

	@Instance(PL2Constants.MODID)
	public static PL2 instance;

	public static CreativeTabs creativeTab = new CreativeTabs(PL2Constants.NAME) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(PL2Items.etched_plate);
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

		if (PL2Config.sapphireOre) {
			GameRegistry.registerWorldGenerator(new SapphireOreGen(), 1);
			logger.info("Registered Sapphire World Generator");
		} else
			logger.info("Sapphire Ore Generation is disabled in the config");

		PL2ASMLoader.init(event);
		MasterInfoRegistry.INSTANCE.init();
		proxy.preInit(event);
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		logger.info("Breaking into the pentagon");
		PL2Crafting.addRecipes();
		logger.info("Registered Crafting Recipes");

		for (EnumDisplayFaceSlot slot : EnumDisplayFaceSlot.values())
			GameRegistry.findRegistry(IPartSlot.class).register(slot);

		OreDictionary.registerOre("oreSapphire", PL2Blocks.sapphire_ore);
		OreDictionary.registerOre("gemSapphire", PL2Items.sapphire);
		OreDictionary.registerOre("dustSapphire", PL2Items.sapphire_dust);
		logger.info("Registered OreDict");

		MinecraftForge.EVENT_BUS.register(new PL2Events());
		logger.info("Registered Event Handlers");
		proxy.load(event);
	}

	@EventHandler
	public void postLoad(FMLPostInitializationEvent evt) {
		logger.info("Please Wait: We are saving Harambe with a time machine");
		proxy.postLoad(evt);
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandResetInfoRegistry());
        MapStorage storage = DimensionManager.getWorld(0).getMapStorage();
		if(storage.getOrLoadData(IdentityCountData.class, IdentityCountData.IDENTIFIER) == null) {
            storage.setData(IdentityCountData.IDENTIFIER, new IdentityCountData());
        }
        if(storage.getOrLoadData(ConnectedDisplayData.class, ConnectedDisplayData.IDENTIFIER) == null) {
            storage.setData(ConnectedDisplayData.IDENTIFIER, new ConnectedDisplayData());
        }
		if(storage.getOrLoadData(GSIData.class, GSIData.IDENTIFIER) == null) {
			storage.setData(GSIData.IDENTIFIER, new GSIData());
		}
	}

	@SubscribeEvent
	public static void registerEntities(RegistryEvent.Register<EntityEntry> e) {
		EntityEntry holographic_display = new EntityEntry(EntityHolographicDisplay.class, "pl2_holographic_display"){

			@Override
			public EntityList.EntityEggInfo getEgg(){ return null; }

			@Override
			public void setEgg(EntityList.EntityEggInfo egg){}

		};

		e.getRegistry().register(holographic_display.setRegistryName(PL2Constants.MODID, "pl2_holographic_display"));
	}

	@EventHandler
	public void serverClose(FMLServerStoppedEvent event) {
		proxy.removeAll();
	}

}