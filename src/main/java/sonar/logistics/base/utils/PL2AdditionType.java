package sonar.logistics.base.utils;

public enum PL2AdditionType {

	PLAYER_ADDED,
	CHUNK_LOADED,
	NETWORK_CONNECTED;
	
	public boolean isNetworkEvent(){
		return this == NETWORK_CONNECTED;
	}
	
}
