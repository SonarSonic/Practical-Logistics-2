package sonar.logistics.api.networks;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.MonitoredList;

public interface INetworkHandler {
	
	public String id();
	
	public int updateRate();
	
	public INetworkChannels instance(ILogisticsNetwork network);
}
