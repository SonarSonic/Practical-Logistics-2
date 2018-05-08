package sonar.logistics.api.core.tiles.connections;

public enum EnumCableConnection {
	VISUAL, //
	NETWORK, //
	NONE;//

	public boolean canConnect() {
		return this == NETWORK;
	}

	public boolean canShowConnection() {
		return this == VISUAL || canConnect();
	}
	
	public boolean matches(EnumCableConnection type){
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