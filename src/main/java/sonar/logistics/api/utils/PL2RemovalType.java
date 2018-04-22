package sonar.logistics.api.utils;

public enum PL2RemovalType {

	PLAYER_REMOVED,
	CHUNK_UNLOADED,
	NETWORK_DISCONNECTED;
	
	public boolean isNetworkEvent(){
		return this == NETWORK_DISCONNECTED;
	}
}
