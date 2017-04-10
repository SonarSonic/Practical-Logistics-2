package sonar.logistics.api.networks;

import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.utils.MonitoredList;

public interface IEntityMonitorHandler<I extends IMonitorInfo> extends INetworkHandler {

	public MonitoredList<I> updateInfo(MonitoredList<I> newList, MonitoredList<I> previousList, EntityConnection entity);

	public String id();
}
