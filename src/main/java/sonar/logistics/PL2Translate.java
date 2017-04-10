package sonar.logistics;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import sonar.core.utils.Localisation;

public class PL2Translate {
	public static final List<Localisation> locals = Lists.newArrayList();

	// ITEMS
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

	// BLOCKS
	public static final Localisation HAMMER = b(PL2Blocks.hammer);
	public static final Localisation HAMMER_AIR = b(PL2Blocks.hammer_air);
	public static final Localisation SAPPHIRE_ORE = b(PL2Blocks.sapphire_ore);

	/// BUTTONS
	public static final Localisation BUTTON_EDIT = t("pl.button.edit");
	public static final Localisation BUTTON_SOURCE = t("pl.button.source");
	public static final Localisation BUTTON_DATA = t("pl.button.data");
	public static final Localisation BUTTON_NAME = t("pl.button.name");
	public static final Localisation BUTTON_PREFIX = t("pl.button.prefix");
	public static final Localisation BUTTON_SUFFIX = t("pl.button.suffix");
	public static final Localisation BUTTON_RESET = t("pl.button.reset");
	public static final Localisation BUTTON_DELETE = t("pl.button.delete");
	public static final Localisation BUTTON_CLEAR = t("pl.button.clear");
	public static final Localisation BUTTON_CLEAR_ALL = t("pl.button.clearall");
	public static final Localisation BUTTON_SAVE = t("pl.button.save");
	public static final Localisation BUTTON_LOCKED = t("pl.button.locked");
	public static final Localisation BUTTON_LAYOUT = t("pl.button.layout");
	public static final Localisation BUTTON_CHANNELS = t("pl.button.channels");
	public static final Localisation BUTTON_HELP_ENABLED = t("pl.button.helpenabled");
	public static final Localisation BUTTON_TARGET = t("pl.button.target");
	public static final Localisation BUTTON_NEAREST = t("pl.button.nearest");
	public static final Localisation BUTTON_FURTHEST = t("pl.button.furthest");
	public static final Localisation BUTTON_SORTING_ORDER = t("pl.button.order");
	public static final Localisation BUTTON_STORED = t("pl.button.stored");
	public static final Localisation BUTTON_BACK = t("pl.button.back");
	public static final Localisation BUTTON_CONFIGURE_FILTERS = t("pl.button.configurefilters");
	public static final Localisation BUTTON_DUMP_PLAYER = t("pl.button.dumpPlayer");
	public static final Localisation BUTTON_DUMP_NETWORK = t("pl.button.dumpNetwork");
	public static final Localisation BUTTON_MOVE_UP = t("pl.button.moveUp");
	public static final Localisation BUTTON_MOVE_DOWN = t("pl.button.moveDown");
	
	///FILTERS
	public static final Localisation FILTERS_ITEM_FILTER = t("pl.filters.newItem");
	public static final Localisation FILTERS_ORE_FILTER = t("pl.filters.newOreDict");
	public static final Localisation FILTERS_FLUID_FILTER = t("pl.filters.newFluid");
	
	
	///CHANNELS
	public static final Localisation CHANNELS_SELECTION = t("pl.channels.selection");
	public static final Localisation CHANNELS_SELECTION_HELP = t("pl.channels.selection.help");
	
	/// GUIDE
	public static final Localisation GUIDE_TITLE = t("pl.guide.title");
	
	/// DISPLAY SCREEN
	public static final Localisation SCREEN_INFO_SELECT = t("pl.display.infoselect");
	public static final Localisation SCREEN_INFO_SELECT_HELP = t("pl.display.infoselect.help");
	public static final Localisation SCREEN_CUSTOM_DATA = t("pl.display.customdata");
	public static final Localisation SCREEN_NO_DATA = t("pl.display.nodata");	
	
	/// DATA EMITTER
	public static final Localisation DATA_EMITTER_PUBLIC = t("pl.emitter.public");
	public static final Localisation DATA_EMITTER_PRIVATE = t("pl.emitter.private");

	/// INFO READER
	public static final Localisation INFO_READER_HELP = t("pl.info.reader.help");
	
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
	
	/// DATA RECEIVER
	public static final Localisation DATA_RECEIVER_HELP = t("pl.receiver.help");
	
	/// ENTITY MODE
	public static final Localisation ENTITY_NODE_RANGE = t("pl.entitynode.range");
	
	/// WIRELESS STORAGE READER
	public static final Localisation WIRELESS_STORAGE_READER_EMITTER = t("pl.wirelessStorage.emitter");
	
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
