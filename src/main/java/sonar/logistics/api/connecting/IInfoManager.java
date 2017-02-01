package sonar.logistics.api.connecting;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import sonar.logistics.api.displays.ConnectedDisplayScreen;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.readers.ILogicMonitor;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface IInfoManager {
	
	public LinkedHashMap<UUID, ILogicMonitor> getMonitors();
	
	public LinkedHashMap<InfoUUID, IMonitorInfo> getInfoList();
	
	public ConcurrentHashMap<Integer, ConnectedDisplayScreen> getConnectedDisplays();
	
	public <T extends IMonitorInfo> MonitoredList<T> getMonitoredList(int networkID, InfoUUID uuid);
	
	public void addMonitor(ILogicMonitor monitor);

	public void removeMonitor(ILogicMonitor monitor);
	
	public void removeAll();
}
