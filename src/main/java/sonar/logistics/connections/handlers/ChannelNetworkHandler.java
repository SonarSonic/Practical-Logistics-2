package sonar.logistics.connections.handlers;

import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.NetworkHandler;
import sonar.logistics.api.asm.NetworkHandlerField;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.info.types.MonitoredBlockCoords;

@NetworkHandler(handlerID = ChannelNetworkHandler.id, modid = PL2Constants.MODID)
public class ChannelNetworkHandler extends ListNetworkHandler<MonitoredBlockCoords> implements ITileMonitorHandler<MonitoredBlockCoords> {
	
	@NetworkHandlerField(handlerID = ChannelNetworkHandler.id)
	public static ChannelNetworkHandler INSTANCE;

	public static final String id = "channels";
	
	@Override
	public String id() {
		return id;
	}

	@Override
	public MonitoredList<MonitoredBlockCoords> updateInfo(MonitoredList<MonitoredBlockCoords> newList, MonitoredList<MonitoredBlockCoords> info, BlockConnection connection) {		
		return info;
	}

	@Override
	public int updateRate() {
		return 0;
	}

}
