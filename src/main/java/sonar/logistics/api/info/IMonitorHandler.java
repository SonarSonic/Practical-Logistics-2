package sonar.logistics.api.info;

import net.minecraft.entity.Entity;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface IMonitorHandler<I extends IMonitorInfo> {

	public MonitoredList<I> updateInfo(ILogisticsNetwork network, MonitoredList<I> previousList, Entity entity);

	public String id();
}
