package sonar.logistics.api.networks;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.utils.MonitoredList;

public interface ITileMonitorHandler<I extends IInfo, C extends INetworkChannels> extends INetworkHandler {

	public MonitoredList<I> updateInfo(C channels, MonitoredList<I> newList, MonitoredList<I> previousList, BlockConnection connection);
	
	public String id();
}
