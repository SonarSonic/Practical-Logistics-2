package sonar.logistics.api.readers;

import sonar.core.utils.IUUIDIdentity;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.IMonitorInfo;

public interface IInfoProvider extends ILogicTile, IUUIDIdentity {

	public IMonitorInfo getMonitorInfo(int pos);
	
	public String getDisplayName();

	public int getMaxInfo();

	public INetworkCache getNetwork();
}
