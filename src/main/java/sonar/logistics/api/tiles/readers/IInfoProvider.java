package sonar.logistics.api.tiles.readers;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.networking.ILogisticsNetwork;
import sonar.logistics.api.viewers.ILogicListenable;

public interface IInfoProvider extends ILogicListenable {
	
	IInfo getMonitorInfo(int pos);

	int getMaxInfo();

	ILogisticsNetwork getNetwork();
}
