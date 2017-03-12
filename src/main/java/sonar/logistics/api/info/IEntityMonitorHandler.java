package sonar.logistics.api.info;

import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface IEntityMonitorHandler<I extends IMonitorInfo> {

	public MonitoredList<I> updateInfo(INetworkCache network, MonitoredList<I> previousList, EntityConnection entity);

	public String id();
}
