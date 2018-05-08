package sonar.logistics.api.core.tiles.readers;

import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.base.listeners.ILogicListenable;

public interface IInfoProvider extends ILogicListenable {
	
	IInfo getMonitorInfo(int pos);

	int getMaxInfo();

	ILogisticsNetwork getNetwork();
}
