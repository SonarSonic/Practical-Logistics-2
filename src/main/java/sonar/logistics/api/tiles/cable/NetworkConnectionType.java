package sonar.logistics.api.tiles.cable;

public enum NetworkConnectionType {
	VISUAL, NETWORK, NONE;

	public boolean canConnect() {
		return this == NETWORK;
	}

	public boolean canShowConnection() {
		return this == VISUAL || canConnect();
	}
}