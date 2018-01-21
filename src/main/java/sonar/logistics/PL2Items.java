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
	//public static Item cable, node, entity_node, array, transfer_node, transceiver, ;
	//public static Item info_reader, inventory_reader, fluid_reader, energy_reader;
	//public static Item display_screen, large_display_screen, holographic_display, data_emitter, data_receiver, redstone_signaller, clock;
	
	public static void registerItems() {
		guide = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemGuide(), "PLGuide"));
		operator = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemOperator(), "Operator"));
		sapphire = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("Sapphire"));
		sapphire_dust = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("SapphireDust"));
		stone_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("StonePlate"));
		signalling_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("SignallingPlate"));
		wireless_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("WirelessPlate"));
		etched_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("EtchedPlate"));
		

		transceiver = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessItemTransceiver().setMaxStackSize(1), "Transceiver"));
		entity_transceiver = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessEntityTransceiver().setMaxStackSize(1), "EntityTransceiver"));
		wireless_storage_reader = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessStorageReader(), "WirelessStorage"));

		/*
		info_reader = register(new SonarRegistryItem(new ItemSidedMultipart(InfoReaderPart.class), "InfoReader"));
		inventory_reader = register(new SonarRegistryItem(new ItemSidedMultipart(InventoryReaderPart.class), "InventoryReader"));
		fluid_reader = register(new SonarRegistryItem(new ItemSidedMultipart(FluidReaderPart.class), "FluidReader"));
		energy_reader = register(new SonarRegistryItem(new ItemSidedMultipart(EnergyReaderPart.class), "EnergyReader"));

		display_screen = register(new SonarRegistryItem(new ItemScreenMultipart(DisplayScreenPart.class), "DisplayScreen"));
		large_display_screen = register(new SonarRegistryItem(new ItemScreenMultipart(LargeDisplayScreenPart.class), "LargeDisplayScreen"));
		holographic_display = register(new SonarRegistryItem(new ItemScreenMultipart(HolographicDisplayPart.class), "HolographicDisplay"));
		data_emitter = register(new SonarRegistryItem(new ItemWirelessMultipart(DataEmitterPart.class), "DataEmitter"));
		data_receiver = register(new SonarRegistryItem(new ItemWirelessMultipart(DataReceiverPart.class), "DataReceiver"));
		redstone_signaller = register(new SonarRegistryItem(new ItemSidedMultipart(RedstoneSignallerPart.class), "RedstoneSignaller"));
		clock = register(new SonarRegistryItem(new ItemSidedMultipart(ClockPart.class), "Clock"));
		*/

	}

	public static void registerMultiparts() {
		/*
		MultipartRegistry.registerPart(DataCablePart.class, PL2Constants.MODID + ":DataCable");
		MultipartRegistry.registerPart(NodePart.class, PL2Constants.MODID + ":Node");
		MultipartRegistry.registerPart(EntityNodePart.class, PL2Constants.MODID + ":EntityNode");
		MultipartRegistry.registerPart(TransferNodePart.class, PL2Constants.MODID + ":TransferNode");
		MultipartRegistry.registerPart(ArrayPart.class, PL2Constants.MODID + ":Array");
		MultipartRegistry.registerPart(InventoryReaderPart.class, PL2Constants.MODID + ":InventoryReader");
		MultipartRegistry.registerPart(FluidReaderPart.class, PL2Constants.MODID + ":FluidReader");
		MultipartRegistry.registerPart(InfoReaderPart.class, PL2Constants.MODID + ":InfoReader");
		MultipartRegistry.registerPart(DataEmitterPart.class, PL2Constants.MODID + ":DataEmitter");
		MultipartRegistry.registerPart(DataReceiverPart.class, PL2Constants.MODID + ":DataReceiver");
		MultipartRegistry.registerPart(DisplayScreenPart.class, PL2Constants.MODID + ":DisplayScreen");
		MultipartRegistry.registerPart(HolographicDisplayPart.class, PL2Constants.MODID + ":HolographicDisplay");
		MultipartRegistry.registerPart(LargeDisplayScreenPart.class, PL2Constants.MODID + ":LargeDisplayScreen");
		MultipartRegistry.registerPart(EnergyReaderPart.class, PL2Constants.MODID + ":EnergyReader");
		MultipartRegistry.registerPart(RedstoneSignallerPart.class, PL2Constants.MODID + ":RedstoneSignaller");
		MultipartRegistry.registerPart(ClockPart.class, PL2Constants.MODID + ":Clock");
		*/
	}
}
