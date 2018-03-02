package sonar.logistics;

import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.item.Item;
import sonar.core.SonarCore;
import sonar.core.SonarRegister;
import sonar.core.registries.ISonarRegistryItem;
import sonar.core.registries.SonarRegistryItem;
import sonar.logistics.common.items.ItemDefaultMultipart;
import sonar.logistics.common.items.ItemGuide;
import sonar.logistics.common.items.ItemOperator;
import sonar.logistics.common.items.ItemScreenMultipart;
import sonar.logistics.common.items.ItemSidedMultipart;
import sonar.logistics.common.items.ItemWirelessMultipart;
import sonar.logistics.common.items.WirelessEntityTransceiver;
import sonar.logistics.common.items.WirelessItemTransceiver;
import sonar.logistics.common.items.WirelessStorageReader;
import sonar.logistics.common.multiparts.displays.DataCablePart;
import sonar.logistics.common.multiparts.displays.DisplayScreenPart;
import sonar.logistics.common.multiparts.displays.HolographicDisplayPart;
import sonar.logistics.common.multiparts.displays.LargeDisplayScreenPart;
import sonar.logistics.common.multiparts.misc.ClockPart;
import sonar.logistics.common.multiparts.misc.RedstoneSignallerPart;
import sonar.logistics.common.multiparts.nodes.ArrayPart;
import sonar.logistics.common.multiparts.nodes.EntityNodePart;
import sonar.logistics.common.multiparts.nodes.NodePart;
import sonar.logistics.common.multiparts.nodes.TransferNodePart;
import sonar.logistics.common.multiparts.readers.EnergyReaderPart;
import sonar.logistics.common.multiparts.readers.FluidReaderPart;
import sonar.logistics.common.multiparts.readers.InfoReaderPart;
import sonar.logistics.common.multiparts.readers.InventoryReaderPart;
import sonar.logistics.common.multiparts.wireless.DataEmitterPart;
import sonar.logistics.common.multiparts.wireless.DataReceiverPart;

public class PL2Items extends PL2 {

	public static Item guide, operator, sapphire, sapphire_dust, stone_plate, etched_plate;
	public static Item cable, node, entity_node, array, transfer_node, transceiver, entity_transceiver, wireless_storage_reader;
	public static Item info_reader, inventory_reader, fluid_reader, energy_reader;
	public static Item display_screen, large_display_screen, holographic_display, data_emitter, data_receiver, redstone_signaller, clock;

	public static void registerItems() {
		guide = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemGuide(), "PLGuide"));
		operator = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemOperator(), "Operator"));
		sapphire = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("Sapphire"));
		sapphire_dust = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("SapphireDust"));
		stone_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("StonePlate"));
		etched_plate = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem("EtchedPlate"));

		cable = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemDefaultMultipart(DataCablePart.class), "DataCable"));
		node = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(NodePart.class), "Node"));
		entity_node = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(EntityNodePart.class), "EntityNode"));
		array = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(ArrayPart.class), "Array"));
		transfer_node = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(TransferNodePart.class), "TransferNode"));
		transceiver = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessItemTransceiver().setMaxStackSize(1), "Transceiver"));
		entity_transceiver = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessEntityTransceiver().setMaxStackSize(1), "EntityTransceiver"));
		wireless_storage_reader = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new WirelessStorageReader(), "WirelessStorage"));

		info_reader = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(InfoReaderPart.class), "InfoReader"));
		inventory_reader = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(InventoryReaderPart.class), "InventoryReader"));
		fluid_reader = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(FluidReaderPart.class), "FluidReader"));
		energy_reader = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(EnergyReaderPart.class), "EnergyReader"));

		display_screen = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemScreenMultipart(DisplayScreenPart.class), "DisplayScreen"));
		large_display_screen = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemScreenMultipart(LargeDisplayScreenPart.class), "LargeDisplayScreen"));
		holographic_display = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemScreenMultipart(HolographicDisplayPart.class), "HolographicDisplay"));
		data_emitter = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemWirelessMultipart(DataEmitterPart.class), "DataEmitter"));
		data_receiver = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemWirelessMultipart(DataReceiverPart.class), "DataReceiver"));
		redstone_signaller = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(RedstoneSignallerPart.class), "RedstoneSignaller"));
		clock = SonarRegister.addItem(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryItem(new ItemSidedMultipart(ClockPart.class), "Clock"));

	}

	public static void registerMultiparts() {
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
	}
}
