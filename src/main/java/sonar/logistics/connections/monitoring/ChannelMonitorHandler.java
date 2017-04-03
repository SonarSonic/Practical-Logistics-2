package sonar.logistics.connections.monitoring;

import sonar.logistics.PL2;
import sonar.logistics.api.asm.TileMonitorHandler;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.nodes.BlockConnection;

@TileMonitorHandler(handlerID = ChannelMonitorHandler.id, modid = PL2.MODID)
public class ChannelMonitorHandler extends LogicMonitorHandler<MonitoredBlockCoords> implements ITileMonitorHandler<MonitoredBlockCoords> {

	public static final String id = "channels";
	
	@Override
	public String id() {
		return id;
	}

	@Override
	public MonitoredList<MonitoredBlockCoords> updateInfo(INetworkCache network, MonitoredList<MonitoredBlockCoords> info, BlockConnection connection) {
		
		return info;
	}

}
