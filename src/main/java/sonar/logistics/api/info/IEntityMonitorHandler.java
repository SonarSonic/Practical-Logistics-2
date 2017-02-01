package sonar.logistics.api.info;

import net.minecraft.entity.Entity;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface IEntityMonitorHandler<I extends IMonitorInfo> {

	public MonitoredList<I> updateInfo(INetworkCache network, MonitoredList<I> previousList, Entity entity);

	public String id();
}
