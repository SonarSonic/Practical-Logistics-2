package sonar.logistics;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class LogisticsConfig extends Logistics {

	public static boolean sapphireOre;
	public static boolean displayMana;
	public static int updateRate;
	//public static int infoUpdateRate;

	public static void initConfiguration(FMLPreInitializationEvent event) {
		loadMainConfig();
	}

	public static void loadMainConfig() {
		Configuration config = new Configuration(new File("config/Practical-Logistics/Main-Config.cfg"));
		config.load();
		updateRate = config.getInt("Network Update Rate", "settings", 0, 0, 100, "how frequently to update networks");
		
		//infoUpdateRate = config.getInt("Info Update Rate", "settings", 50, 0, 100, "how frequently to update info - (info shown in Info Reader)");
		
		sapphireOre = config.getBoolean("Generate Ore", "settings", true, "Sapphire Ore");
		displayMana = config.getBoolean("Mana", "settings", false, "Display Mana Percentage");
		config.save();
	}
}
