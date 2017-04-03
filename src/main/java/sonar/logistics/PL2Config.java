package sonar.logistics;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class PL2Config extends PL2 {

	public static boolean displayMana;
	public static int updateRate;
	public static boolean sapphireOre;
	public static int sapphireMinVeinSize, sapphireMaxVeinSize, sapphireChance, sapphireMinY, sapphireMaxY;

	public static void initConfiguration(FMLPreInitializationEvent event) {
		loadMainConfig();
	}

	public static void loadMainConfig() {
		Configuration config = new Configuration(new File("config/Practical-Logistics/Main-Config.cfg"));
		config.load();
		updateRate = config.getInt("Network Update Rate", "settings", 0, 0, 100, "how frequently to update networks, increase if server is lagging");

		sapphireOre = config.getBoolean("Generate Ore", "sapphire_ore", true, "Should Sapphire Ore be spawned in the world.");
		sapphireMinVeinSize = config.getInt("Min Vein Size", "sapphire_ore", 2, 1, 500, "the smallest amount of sapphire found in one vein");
		sapphireMaxVeinSize = config.getInt("Max Vein Size", "sapphire_ore", 8, 1, 500, "the largest amount of sapphire found in one vein");
		sapphireChance = config.getInt("Chance", "sapphire_ore", 15, 1, 500, "the chance of a sapphire ore spawning");
		sapphireMinY = config.getInt("Min Y", "sapphire_ore", 2, 1, 500, "the minimum Y coord where this will spawn");
		sapphireMaxY = config.getInt("Max Y", "sapphire_ore", 16, 1, 500, "the maximum Y coord where this will spawn");

		//displayMana = config.getBoolean("Mana", "settings", false, "Display Mana Percentage");
		config.save();
	}
}
