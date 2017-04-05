package sonar.logistics.api.readers;

import sonar.core.utils.Localisation;
import sonar.logistics.PL2Translate;

public class RedstoneSignaller {

	public enum StatementType {
		DEFAULT, OVERRIDE;

		Localisation name;

		StatementType() {
			name = PL2Translate.get("pl.signaller." + name().toLowerCase());
		}

		public String getClientName() {
			return name.t();
		}
	}

	public enum StatementSetting {
		ALL, ONE;

		Localisation name;

		StatementSetting() {
			name = PL2Translate.get("pl.signaller." + name().toLowerCase());
		}

		public String getClientName() {
			return name.t();
		}
	}

}
