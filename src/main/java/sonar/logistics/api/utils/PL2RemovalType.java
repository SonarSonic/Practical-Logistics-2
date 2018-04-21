package sonar.logistics.api.utils;

public enum PL2RemovalType {

	NETWORK_DISCONNECTED,
	CHUNK_UNLOADED,
	PLAYER_REMOVED;	
	
	public boolean isNetworkEvent(){
		return this == NETWORK_DISCONNECTED;
	}
}
