package sonar.logistics.api.utils;

public enum PL2AdditionType {

	NETWORK_CONNECTED,
	CHUNK_LOADED,
	PLAYER_ADDED;
	
	public boolean isNetworkEvent(){
		return this == NETWORK_CONNECTED;
	}
	
}
