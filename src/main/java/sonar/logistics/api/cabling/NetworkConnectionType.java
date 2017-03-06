package sonar.logistics.api.cabling;

public enum NetworkConnectionType {
	VISUAL, NETWORK, NONE;

	public boolean canConnect() {
		return this == NETWORK;
	}

	public boolean canShowConnection() {
		return this == VISUAL || canConnect();
	}
}