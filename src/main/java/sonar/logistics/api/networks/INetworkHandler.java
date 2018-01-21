package sonar.logistics.api.networks;

import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.readers.IListReader;

public interface INetworkHandler {
	
	//public String id();
	public default int getReaderID(IListReader reader) {
		return 0;
	}
	
	public default InfoUUID getReaderUUID(IListReader reader){
		return new InfoUUID(reader.getIdentity(), getReaderID(reader));
	}
		
	public int updateRate();
	
	public Class<? extends INetworkChannels> getChannelsType();
}
