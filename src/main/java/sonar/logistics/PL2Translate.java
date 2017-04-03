package sonar.logistics;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import sonar.core.utils.Localisation;

public class PL2Translate {
	// blocks
	public static final ArrayList<Localisation> locals = new ArrayList();

	public static final Localisation GUIDE = i(PL2Items.guide);
	public static final Localisation OPERATOR = i(PL2Items.operator);
	public static final Localisation SAPPHIRE = i(PL2Items.sapphire);
	public static final Localisation SAPPHIRE_DUST = i(PL2Items.sapphire_dust);
	public static final Localisation STONE_PLATE = i(PL2Items.stone_plate);
	public static final Localisation ETCHED_PLATE = i(PL2Items.etched_plate);
	public static final Localisation CABLE = i(PL2Items.cable);
	public static final Localisation NODE = i(PL2Items.node);
	public static final Localisation ENTITY_NODE = i(PL2Items.entity_node);
	public static final Localisation ARRAY = i(PL2Items.array);
	public static final Localisation TRANSFER_NODE = i(PL2Items.transfer_node);
	public static final Localisation TRANSCEIVER = i(PL2Items.transceiver);
	public static final Localisation ENTITY_TRANSCEIVER = i(PL2Items.entity_transceiver);
	public static final Localisation WIRELESS_STORAGE_READER = i(PL2Items.wireless_storage_reader);
	public static final Localisation INFO_READER = i(PL2Items.info_reader);
	public static final Localisation INVENTORY_READER = i(PL2Items.inventory_reader);
	public static final Localisation FLUID_READER = i(PL2Items.fluid_reader);
	public static final Localisation ENERGY_READER = i(PL2Items.energy_reader);
	public static final Localisation DISPLAY_SCREEN = i(PL2Items.display_screen);
	public static final Localisation LARGE_DISPLAY_SCREEN = i(PL2Items.large_display_screen);
	public static final Localisation HOLOGRAPHIC_DISPLAY = i(PL2Items.holographic_display);
	public static final Localisation DATA_EMITTER = i(PL2Items.data_emitter);
	public static final Localisation DATA_RECEIVER = i(PL2Items.data_receiver);
	public static final Localisation REDSTONE_SIGNALLER = i(PL2Items.redstone_signaller);
	public static final Localisation CLOCK = i(PL2Items.clock);

	public static final Localisation HAMMER = b(PL2Blocks.hammer);
	public static final Localisation HAMMER_AIR = b(PL2Blocks.hammer_air);
	public static final Localisation SAPPHIRE_ORE = b(PL2Blocks.sapphire_ore);

	/// DATA EMITTER
	public static final Localisation DATA_EMITTER_PUBLIC = t("pl.emitter.public");
	public static final Localisation DATA_EMITTER_PRIVATE = t("pl.emitter.private");

	/// FLUID READER
	public static final Localisation FLUID_READER_TANKS_D = t("pl.fluid.desc.tanks");
	public static final Localisation FLUID_READER_TANKS_N = t("pl.fluid.mode.tanks");
	public static final Localisation FLUID_READER_SELECTED_D = t("pl.fluid.desc.selected");
	public static final Localisation FLUID_READER_SELECTED_N = t("pl.fluid.mode.selected");
	public static final Localisation FLUID_READER_POS_D = t("pl.fluid.desc.pos");
	public static final Localisation FLUID_READER_POS_N = t("pl.fluid.mode.pos");
	public static final Localisation FLUID_READER_STORAGE_D = t("pl.fluid.desc.storage");
	public static final Localisation FLUID_READER_STORAGE_N = t("pl.fluid.mode.storage");
	public static final Localisation FLUID_READER_STORED = t("pl.fluid.sort.stored");
	public static final Localisation FLUID_READER_NAME = t("pl.fluid.sort.name");
	public static final Localisation FLUID_READER_MODID = t("pl.fluid.sort.modid");
	public static final Localisation FLUID_READER_TEMPERATURE = t("pl.fluid.sort.temperature");
	
	/// INVENTORY READER
	public static final Localisation INV_READER_INVENTORIES_N = t("pl.inv.mode.inventories");
	public static final Localisation INV_READER_STACK_N = t("pl.inv.mode.stack");
	public static final Localisation INV_READER_SLOT_N = t("pl.inv.mode.slot");
	public static final Localisation INV_READER_POS_N = t("pl.inv.mode.pos");
	public static final Localisation INV_READER_STORAGE_N = t("pl.inv.mode.storage");
	public static final Localisation INV_READER_FILTERED_N = t("pl.inv.mode.filtered");
	public static final Localisation INV_READER_STORED = t("pl.inv.sort.stored");
	public static final Localisation INV_READER_NAME = t("pl.inv.sort.name");
	public static final Localisation INV_READER_MODID = t("pl.inv.sort.modid");
	
	/// REDSTONE SIGNALLER
	public static final Localisation RED_SIGNALLER_DEFAULT = t("pl.signaller.default");
	public static final Localisation RED_SIGNALLER_OVERRIDE = t("pl.signaller.override");
	public static final Localisation RED_SIGNALLER_ALL = t("pl.signaller.all");
	public static final Localisation RED_SIGNALLER_ONE = t("pl.signaller.one");
	

	public static Localisation get(String original) {
		for (Localisation l : locals) {
			if (l.o().equals(original)) {
				return l;
			}
		}
		return new Localisation(original);
	}

	public static Localisation t(String s) {
		Localisation l = new Localisation(s);
		locals.add(l);
		return l;
	}

	public static Localisation i(Item item) {
		Localisation l = new Localisation(item.getUnlocalizedName() + ".name");
		locals.add(l);
		return l;
	}

	public static Localisation b(Block block) {
		Localisation l = new Localisation(block.getUnlocalizedName() + ".name");
		locals.add(l);
		return l;
	}
}
