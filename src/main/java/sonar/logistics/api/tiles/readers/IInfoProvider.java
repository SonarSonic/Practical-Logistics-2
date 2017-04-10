package sonar.logistics.api.tiles.readers;

import sonar.core.listener.PlayerListener;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.viewers.ILogicListenable;

public interface IInfoProvider extends ILogicListenable<PlayerListener> {
	
	public IMonitorInfo getMonitorInfo(int pos);
	
	public String getDisplayName();

	public int getMaxInfo();

	public ILogisticsNetwork getNetwork();
}
