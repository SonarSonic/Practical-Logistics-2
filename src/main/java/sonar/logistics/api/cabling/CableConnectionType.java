package sonar.logistics.api.cabling;

public enum CableConnectionType {
	VISUAL, //
	NETWORK, //
	NONE;//

	public boolean canConnect() {
		return this == NETWORK;
	}

	public boolean canShowConnection() {
		return this == VISUAL || canConnect();
	}
	
	public boolean matches(CableConnectionType type){
		switch(this){
		case NETWORK:
			return type.canConnect();
		case VISUAL:
			return type.canShowConnection();
		default:
			return this == type;		
		}
	}
}