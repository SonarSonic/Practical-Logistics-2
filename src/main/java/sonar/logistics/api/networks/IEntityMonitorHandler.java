package sonar.logistics.api.networks;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.utils.MonitoredList;

public interface IEntityMonitorHandler<I extends IInfo, C extends INetworkChannels> extends INetworkHandler {

	public MonitoredList<I> updateInfo(C channels, MonitoredList<I> newList, MonitoredList<I> previousList, EntityConnection entity);

	public String id();
}
