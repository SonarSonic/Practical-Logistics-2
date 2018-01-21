package sonar.logistics.api.wireless;

public enum EnumConnected {
	CONNECTED, DISCONNECTED;

	public boolean isConnected() {
		return this == CONNECTED;
	}

	public boolean isDisconnected() {
		return this == DISCONNECTED;
	}

	public EnumConnected invert() {
		switch (this) {
		case CONNECTED:
			return DISCONNECTED;
		default:
			return CONNECTED;
		}
	}

	public boolean isMatching(EnumConnected connected) {
		return connected == this;
	}

	public static EnumConnected fromBoolean(boolean isConnected) {
		return isConnected ? CONNECTED : DISCONNECTED;
	}

	public static EnumConnected isConnected(EnumConnected... connections) {
		for (EnumConnected connect : connections) {
			if (connect.isDisconnected()) {
				return DISCONNECTED;
			}
		}
		return CONNECTED;
	}
}
