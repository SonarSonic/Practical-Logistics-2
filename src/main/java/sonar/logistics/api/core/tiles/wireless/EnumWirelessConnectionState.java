package sonar.logistics.api.core.tiles.wireless;

public enum EnumWirelessConnectionState {
	CONNECTED, DISCONNECTED;

	public boolean isConnected() {
		return this == CONNECTED;
	}

	public boolean isDisconnected() {
		return this == DISCONNECTED;
	}

	public EnumWirelessConnectionState invert() {
		switch (this) {
		case CONNECTED:
			return DISCONNECTED;
		default:
			return CONNECTED;
		}
	}

	public boolean isMatching(EnumWirelessConnectionState connected) {
		return connected == this;
	}

	public static EnumWirelessConnectionState fromBoolean(boolean isConnected) {
		return isConnected ? CONNECTED : DISCONNECTED;
	}

	public static EnumWirelessConnectionState isConnected(EnumWirelessConnectionState... connections) {
		for (EnumWirelessConnectionState connect : connections) {
			if (connect.isDisconnected()) {
				return DISCONNECTED;
			}
		}
		return CONNECTED;
	}
}
