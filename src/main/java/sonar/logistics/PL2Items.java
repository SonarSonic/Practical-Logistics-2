package sonar.logistics;

import net.minecraft.item.Item;
import sonar.core.SonarRegister;
import sonar.core.registries.SonarRegistryItem;
import sonar.logistics.common.items.ItemGuide;
import sonar.logistics.common.items.ItemOperator;
import sonar.logistics.common.items.WirelessEntityTransceiver;
import sonar.logistics.common.items.WirelessItemTransceiver;
import sonar.logistics.common.items.WirelessStorageReader;

public class PL2Items extends PL2 {

	public static Item guide, operator, sapphire, sapphire_dust, stone_plate, etched_plate, signalling_plate, wireless_plate;
	public static Item transceiver, entity_transceiver, wireless_storage_reader;
	
	public static void registerItems() {
		sapphire = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("Sapphire"));
		sapphire_dust = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("SapphireDust"));
		stone_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("StonePlate"));
		signalling_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("SignallingPlate"));
		wireless_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("WirelessPlate"));
		etched_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("EtchedPlate"));
		

		transceiver = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessItemTransceiver().setMaxStackSize(1), "Transceiver"));
		entity_transceiver = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessEntityTransceiver().setMaxStackSize(1), "EntityTransceiver"));
		wireless_storage_reader = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessStorageReader(), "WirelessStorage"));

		operator = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemOperator(), "Operator"));
		guide = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemGuide(), "PLGuide"));

	}
}
