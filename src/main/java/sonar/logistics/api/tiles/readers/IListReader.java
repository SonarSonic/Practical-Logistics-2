package sonar.logistics.api.tiles.readers;

import java.util.List;
import java.util.Map;

import sonar.core.listener.PlayerListener;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.connections.handlers.DefaultNetworkHandler;

public interface IListReader<T extends IMonitorInfo> extends ILogicListenable<PlayerListener>, INetworkTile {

	public MonitoredList<T> sortMonitoredList(MonitoredList<T> updateInfo, int channelID);	
	
	public MonitoredList<T> getUpdatedList(InfoUUID uuid, Map<NodeConnection, MonitoredList<T>> channels, List<NodeConnection> usedChannels);	

	public List<INetworkHandler> getValidHandlers();
}
