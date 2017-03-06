package sonar.logistics.api.info;

import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface ITileMonitorHandler<I extends IMonitorInfo> {

	public MonitoredList<I> updateInfo(INetworkCache network, MonitoredList<I> previousList, BlockConnection connection);
	
	public String id();
}
