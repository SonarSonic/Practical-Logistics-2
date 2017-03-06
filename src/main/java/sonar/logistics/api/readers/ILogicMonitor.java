package sonar.logistics.api.readers;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.entity.Entity;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;

/***/
public interface ILogicMonitor<T extends IMonitorInfo> extends IChannelledTile,IInfoProvider {
		
	/**the instance of the MonitorHandler this LogicMonitor uses*/
	public LogicMonitorHandler<T> getHandler();	
	
	/**this is when the list should be set and added to the ClinetMonitoredLists*/
	public MonitoredList<T> sortMonitoredList(MonitoredList<T> updateInfo, int channelID);
	
	public void setMonitoredInfo(MonitoredList<T> updateInfo, ArrayList<BlockConnection> connections, ArrayList<Entity> entities, int channelID);
	
	public ChannelType channelType();
	
	public int getMaxInfo();
	
	/**the multipart UUID*/
	public UUID getUUID();
	
}
