package sonar.logistics.api.connecting;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.entity.Entity;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.readers.ILogicMonitor;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;

/** implemented on {@link INetworkCache}s which can monitor info, items, fluids etc */
public interface ILogisticsNetwork extends INetworkCache {

	public RefreshType getLastRefresh();

	/** called when a display is connected to the network */
	public void addDisplay(IInfoDisplay display);

	/** called when a display is disconnected from the network */
	public void removeDisplay(IInfoDisplay display);
	
	/** called when a {@link ILogicMonitor} is connected to the network */
	public <T extends IMonitorInfo> void addMonitor(ILogicMonitor<T> monitor);

	/** called when a {@link ILogicMonitor} is disconnected to the network */
	public <T extends IMonitorInfo> void removeMonitor(ILogicMonitor<T> monitor);

	/** gathers the monitored list required by the {@link ILogicMonitor} which is then cached
	 * @return the updated monitored list */
	public <T extends IMonitorInfo> MonitoredList<T> updateMonitoredList(ILogicMonitor<T> monitor, int infoID, Map<BlockConnection, MonitoredList<?>> connections, Map<Entity, MonitoredList<?>> entityConnections, ArrayList<BlockConnection> nodeConnections, ArrayList<Entity> entities);

	/** gets the full monitored list for the Handler type
	 * @param type the type of handler to get a list for
	 * @return a full list of data */
	public <T extends IMonitorInfo> Map<BlockConnection, MonitoredList<?>> getTileMonitoredList(LogicMonitorHandler<T> type);

	public <T extends IMonitorInfo> Map<Entity, MonitoredList<?>> getEntityMonitoredList(LogicMonitorHandler<T> type);

}
