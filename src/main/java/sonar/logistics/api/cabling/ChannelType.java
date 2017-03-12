package sonar.logistics.api.cabling;

/**used to define how many channels a {@link IChannelledTile} can connect to*/
public enum ChannelType {
	/**allows only one channel to be connected. e.g. InfoReader*/
	SINGLE, 
	
	/**allows an unlimited amount of channels to be connected. e.g. InventoryReader*/
	UNLIMITED,
	
	NETWORK_SINGLE;
	
	
}
