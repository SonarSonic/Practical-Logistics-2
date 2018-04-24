package sonar.logistics.api.tiles.readers;

import sonar.core.translate.Localisation;
import sonar.logistics.PL2Translate;

/** all the modes used by the Fluid Reader */
public class FluidReader {

	public enum Modes {
		TANKS, SELECTED, POS, STORAGE;
		Localisation desc, name;

		Modes() {
			desc = PL2Translate.get("pl.fluid.desc." + name().toLowerCase());
			name = PL2Translate.get("pl.fluid.mode." + name().toLowerCase());
		}

		public String getDescription() {
			return desc.t();
		}

		public String getClientName() {
			return name.t();
		}
	}

	public enum SortingType {
		STORED, NAME, MODID, TEMPERATURE;
		Localisation name;

		SortingType() {
			name = PL2Translate.get("pl.fluid.sort." + name().toLowerCase());
		}

		public String getClientName() {
			return name.t();
		}
	}
}
