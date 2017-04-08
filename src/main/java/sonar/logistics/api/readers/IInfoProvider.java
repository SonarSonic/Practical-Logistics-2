package sonar.logistics.api.readers;

import sonar.core.listener.PlayerListener;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.viewers.ILogicViewable;

public interface IInfoProvider extends ILogicViewable<PlayerListener> {
	
	public IMonitorInfo getMonitorInfo(int pos);
	
	public String getDisplayName();

	public int getMaxInfo();

	public ILogisticsNetwork getNetwork();
}
