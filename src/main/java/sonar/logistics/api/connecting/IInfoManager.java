package sonar.logistics.api.connecting;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.World;
import sonar.logistics.api.displays.ConnectedDisplayScreen;
import sonar.logistics.api.displays.ILargeDisplay;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface IInfoManager {

	public LinkedHashMap<UUID, ILogicViewable> getMonitors();

	public LinkedHashMap<InfoUUID, IMonitorInfo> getInfoList();

	public ConcurrentHashMap<Integer, ConnectedDisplayScreen> getConnectedDisplays();

	public <T extends IMonitorInfo> MonitoredList<T> getMonitoredList(int networkID, InfoUUID uuid);

	public ConnectedDisplayScreen getOrCreateDisplayScreen(World world, ILargeDisplay display, int registryID);
	
	public void addMonitor(ILogicViewable monitor);

	public void removeMonitor(ILogicViewable monitor);

	public void removeAll();
}
