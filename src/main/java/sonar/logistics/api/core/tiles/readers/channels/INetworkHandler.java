package sonar.logistics.api.core.tiles.readers.channels;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.readers.IListReader;

public interface INetworkHandler {
	
	//public String id();
	default int getReaderID(IListReader reader) {
		return 0;
	}
	
	default InfoUUID getReaderUUID(IListReader reader){
		return new InfoUUID(reader.getIdentity(), getReaderID(reader));
	}
		
	int updateRate();
	
	Class<? extends INetworkChannels> getChannelsType();
}
