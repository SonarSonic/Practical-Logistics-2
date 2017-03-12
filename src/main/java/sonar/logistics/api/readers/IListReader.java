package sonar.logistics.api.readers;

import java.util.ArrayList;
import java.util.Map;

import sonar.core.utils.IUUIDIdentity;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface IListReader<T extends IMonitorInfo> extends ILogicViewable, ILogicTile, IUUIDIdentity {

	public MonitoredList<T> sortMonitoredList(MonitoredList<T> updateInfo, int channelID);	
	
	public MonitoredList<T> getUpdatedList(int infoID, Map<NodeConnection, MonitoredList<?>> channels, ArrayList<NodeConnection> usedChannels);	

	public LogicMonitorHandler[] getValidHandlers();
}
