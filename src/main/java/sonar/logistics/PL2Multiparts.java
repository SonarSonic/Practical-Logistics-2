package sonar.logistics;

import net.minecraft.item.ItemStack;
import sonar.core.translate.Localisation;

public enum PL2Multiparts {
	ARRAY(0.625, 0.0625 * 1, 0.0625 * 4, PL2Translate.ARRAY) {
		public ItemStack getItem() {
			return new ItemStack(PL2Items.array);
		}
	},
	CLOCK(3 * 0.0625, 0.0625 * 1, 0.0625 * 3, PL2Translate.CLOCK) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.clock);
		}
	},
	DATA_CABLE(0, 0, 0, PL2Translate.CABLE) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.cable);
		}
	},
	DATA_EMITTER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, PL2Translate.DATA_EMITTER) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.data_emitter);
		}
	},
	DATA_RECEIVER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, PL2Translate.DATA_RECEIVER) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.data_receiver);
		}
	},
	DISPLAY_SCREEN(0, 0, 0, PL2Translate.DISPLAY_SCREEN) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.display_screen);
		}
	},
	ENERGY_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 6, PL2Translate.ENERGY_READER) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.energy_reader);
		}
	},
	ENTITY_NODE(5 * 0.0625, 0.0625 * 1, 0.0625 * 4, PL2Translate.ENTITY_NODE) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.entity_node);
		}
	},
	FLUID_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 6, PL2Translate.FLUID_READER) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.fluid_reader);
		}
	},
	INFO_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 6, PL2Translate.INFO_READER) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.info_reader);
		}
	},
	INVENTORY_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 6, PL2Translate.INVENTORY_READER) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.inventory_reader);
		}
	},
	LARGE_DISPLAY_SCREEN(0, 0, 0, PL2Translate.LARGE_DISPLAY_SCREEN) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.large_display_screen);
		}
	},
	NODE(0.875, 0, 0.0625, PL2Translate.NODE) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.node);
		}
	},
	REDSTONE_SIGNALLER(3 * 0.0625, 0.0625 * 1, 0.0625 * 6, PL2Translate.REDSTONE_SIGNALLER) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.redstone_signaller);
		}
	},
	TRANSFER_NODE(0.0625 * 8, 0, 0.0625 * 2, PL2Translate.TRANSFER_NODE) {
		@Override
		public ItemStack getItem() {
			return new ItemStack(PL2Items.transfer_node);
		}
	};

	public double width, heightMin, heightMax;
	private Localisation localisation;

	PL2Multiparts(double width, double heightMin, double heightMax, Localisation localisation) {
		this.width = width;
		this.heightMin = heightMin;
		this.heightMax = heightMax;
		this.localisation = localisation;
	}

	public abstract ItemStack getItem();

	public String getUnlocalisedName() {
		return localisation.o();
	}

	public String getDisplayName() {
		return localisation.t();
	}
}
