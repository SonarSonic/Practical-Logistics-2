package sonar.logistics;

import net.minecraft.item.ItemStack;
import sonar.core.utils.Localisation;

public enum PL2Multiparts {
	ARRAY(0.625, 0.0625 * 1, 0.0625 * 4, new ItemStack(PL2Items.array), PL2Translate.ARRAY),
	CLOCK(3 * 0.0625, 0.0625 * 1, 0.0625 * 3, new ItemStack(PL2Items.clock), PL2Translate.CLOCK),
	DATA_EMITTER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, new ItemStack(PL2Items.data_emitter), PL2Translate.DATA_EMITTER),
	DATA_RECEIVER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, new ItemStack(PL2Items.data_receiver), PL2Translate.DATA_RECEIVER),
	DISPLAY_SCREEN(0, 0, 0, new ItemStack(PL2Items.display_screen), PL2Translate.DISPLAY_SCREEN),
	ENERGY_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 6, new ItemStack(PL2Items.energy_reader), PL2Translate.ENERGY_READER),
	ENTITY_NODE(5 * 0.0625, 0.0625 * 1, 0.0625 * 4, new ItemStack(PL2Items.entity_node), PL2Translate.ENTITY_NODE),
	FLUID_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 6, new ItemStack(PL2Items.fluid_reader), PL2Translate.FLUID_READER),
	INFO_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 6, new ItemStack(PL2Items.info_reader), PL2Translate.INFO_READER),
	INVENTORY_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 6, new ItemStack(PL2Items.inventory_reader), PL2Translate.INVENTORY_READER),
	LARGE_DISPLAY_SCREEN(0, 0, 0, new ItemStack(PL2Items.large_display_screen), PL2Translate.LARGE_DISPLAY_SCREEN),
	NODE(0.875, 0, 0.0625, new ItemStack(PL2Items.node), PL2Translate.NODE),
	REDSTONE_SIGNALLER(3 * 0.0625, 0.0625 * 1, 0.0625 * 6, new ItemStack(PL2Items.redstone_signaller), PL2Translate.REDSTONE_SIGNALLER),
	TRANSFER_NODE(0.0625 * 8, 0, 0.0625 * 2, new ItemStack(PL2Items.transfer_node), PL2Translate.TRANSFER_NODE);

	public double width, heightMin, heightMax;
	public ItemStack stack;
	public Localisation localisation;

	PL2Multiparts(double width, double heightMin, double heightMax, ItemStack stack, Localisation localisation) {
		this.width = width;
		this.heightMin = heightMin;
		this.heightMax = heightMax;
		this.stack = stack;
		this.localisation = localisation;
	}
}
