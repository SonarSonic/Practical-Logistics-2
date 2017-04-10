package sonar.logistics.api.networks;

import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.utils.MonitoredList;

public interface ITileMonitorHandler<I extends IMonitorInfo> extends INetworkHandler {

	public MonitoredList<I> updateInfo(MonitoredList<I> newList, MonitoredList<I> previousList, BlockConnection connection);
	
	public String id();
}
