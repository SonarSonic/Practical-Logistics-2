package sonar.logistics.api.readers;

import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.viewers.ILogicViewable;

public interface IInfoProvider extends ILogicViewable {
	
	public IMonitorInfo getMonitorInfo(int pos);
	
	public String getDisplayName();

	public int getMaxInfo();

	public INetworkCache getNetwork();
}
