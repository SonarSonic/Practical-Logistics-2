package sonar.logistics.api.tiles.readers;

import sonar.core.translate.Localisation;
import sonar.logistics.PL2Translate;

/** all the modes used by the Inventory Reader */
public class InventoryReader {
	public static enum Modes {
		INVENTORIES, STACK, SLOT, POS, STORAGE, FILTERED;

		Localisation name;

		Modes() {
			name = PL2Translate.get("pl.inv.mode." + name().toLowerCase());
		}

		public String getClientName() {
			return name.t();
		}
	}

	public static enum SortingType {
		STORED, NAME, MODID;
		
		Localisation name;

		SortingType() {
			name = PL2Translate.get("pl.inv.sort." + name().toLowerCase());
		}

		public String getClientName() {
			return name.t();
		}
		public SortingType switchDir() {
			switch (this) {
			case STORED:
				return NAME;
			case NAME:
				return MODID;
			default:
				return STORED;
			}
		}
	}
}
