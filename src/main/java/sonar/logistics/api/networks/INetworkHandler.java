package sonar.logistics.api.networks;

public interface INetworkHandler {
	
	//public String id();
	
	public int updateRate();
	
	public Class<? extends INetworkChannels> getChannelsType();
}
