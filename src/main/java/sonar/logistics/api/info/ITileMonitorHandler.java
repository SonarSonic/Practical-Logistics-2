package sonar.logistics.api.info;

import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface ITileMonitorHandler<I extends IMonitorInfo> {

	public MonitoredList<I> updateInfo(INetworkCache network, MonitoredList<I> previousList, NodeConnection connection);
	
	public String id();
}
