package sonar.logistics;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class PL2Config extends PL2 {

	//public static boolean displayMana;
	public static int inventoryUpdate, fluidUpdate, energyUpdate, infoUpdate, transferUpdate;
	public static boolean sapphireOre;
	public static int sapphireMinVeinSize, sapphireMaxVeinSize, sapphireChance, sapphireMinY, sapphireMaxY;

	public static void initConfiguration(FMLPreInitializationEvent event) {
		loadMainConfig();
	}

	public static void loadMainConfig() {
		Configuration config = new Configuration(new File("config/Practical-Logistics/Main-Config.cfg"));
		config.load();
		inventoryUpdate = config.getInt("Inventory Reader Rate", "settings", 20, 0, 100, "how frequently to update inventories, increase if server is lagging");
		fluidUpdate = config.getInt("Fluid Reader Rate", "settings", 10, 0, 100, "how frequently to update fluids, increase if server is lagging");
		energyUpdate = config.getInt("Energy Reader Rate", "settings", 10, 0, 100, "how frequently to update energy, increase if server is lagging");
		infoUpdate = config.getInt("Info Reader Rate", "settings", 20, 0, 100, "how frequently to update info, increase if server is lagging");	
		
		transferUpdate = config.getInt("Transfer Network Rate", "settings", 20, 0, 100, "how frequently to update each Transfer Node, increase if server is lagging");	
		
		sapphireOre = config.getBoolean("Generate Ore", "sapphire_ore", true, "Should Sapphire Ore be spawned in the world.");
		sapphireMinVeinSize = config.getInt("Min Vein Size", "sapphire_ore", 2, 1, 500, "the smallest amount of sapphire found in one vein");
		sapphireMaxVeinSize = config.getInt("Max Vein Size", "sapphire_ore", 8, 1, 500, "the largest amount of sapphire found in one vein");
		sapphireChance = config.getInt("Chance", "sapphire_ore", 15, 1, 500, "the chance of a sapphire ore spawning");
		sapphireMinY = config.getInt("Min Y", "sapphire_ore", 2, 1, 500, "the minimum Y coord where this will spawn");
		sapphireMaxY = config.getInt("Max Y", "sapphire_ore", 16, 1, 500, "the maximum Y coord where this will spawn");
		config.save();
	}
}
