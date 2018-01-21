package sonar.logistics.api.wireless;

import sonar.core.translate.Localisation;
import sonar.logistics.PL2Translate;

public enum WirelessSecurity {
	PUBLIC(PL2Translate.DATA_EMITTER_PUBLIC), PRIVATE(PL2Translate.DATA_EMITTER_PRIVATE);
	public Localisation l;

	WirelessSecurity(Localisation l) {
		this.l = l;
	}

	public boolean requiresPermission() {
		return this == PRIVATE;
	}

	public String getClientName() {
		return l.t();
	}
}
