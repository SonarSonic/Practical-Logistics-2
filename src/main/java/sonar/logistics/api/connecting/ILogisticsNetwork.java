package sonar.logistics.api.connecting;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.entity.Entity;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.readers.IListReader;
import sonar.logistics.api.readers.INetworkReader;
import sonar.logistics.api.readers.IdentifiedChannelsList;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;

/** implemented on {@link INetworkCache}s which can monitor info, items, fluids etc */
public interface ILogisticsNetwork extends INetworkCache {

	public RefreshType getLastRefresh();

	/** called when a display is connected to the network */
	public void addDisplay(IInfoDisplay display);

	/** called when a display is disconnected from the network */
	public void removeDisplay(IInfoDisplay display);
	
	/** called when a {@link INetworkReader} is connected to the network */
	public <T extends IMonitorInfo> void addMonitor(IListReader<T> monitor);

	/** called when a {@link INetworkReader} is disconnected to the network */
	public <T extends IMonitorInfo> void removeMonitor(IListReader<T> monitor);

	/** gets the full monitored list for the Handler type
	 * @param type the type of handler to get a list for
	 * @return a full list of data */
	public <T extends IMonitorInfo> Map<NodeConnection, MonitoredList<?>> getChannels(LogicMonitorHandler<T> type, IdentifiedChannelsList channels);

	//public <T extends IMonitorInfo> Map<EntityConnection, MonitoredList<?>> getEntityMonitoredList(LogicMonitorHandler<T> type, IdentifiedChannelsList channels);

}
