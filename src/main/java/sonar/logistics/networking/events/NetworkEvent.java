package sonar.logistics.networking.events;

import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.logistics.api.networking.ILogisticsNetwork;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.readers.IInfoProvider;

/**server side only*/
public class NetworkEvent extends Event {
	
	public final ILogisticsNetwork network;
	
	public NetworkEvent(ILogisticsNetwork network) {
		super();
		this.network = network;
	}
	
	public static class ConnectedNetwork extends NetworkEvent{
		
		public final ILogisticsNetwork connected_network;
		
		public ConnectedNetwork(ILogisticsNetwork network, ILogisticsNetwork connected){
			super(network);
			connected_network = connected;
		}
		
	}
	
	public static class DisconnectedNetwork extends NetworkEvent{
		
		public final ILogisticsNetwork disconnected_network;
		
		public DisconnectedNetwork(ILogisticsNetwork network, ILogisticsNetwork connected){
			super(network);
			disconnected_network = connected;
		}
		
	}
	
	public static class ConnectedTile extends NetworkEvent{
				
		public final INetworkTile tile;
		
		public ConnectedTile(ILogisticsNetwork network, INetworkTile tile){
			super(network);
			this.tile = tile;
		}
		
	}
	
	public static class DisconnectedTile extends NetworkEvent{
				
		public final INetworkTile tile;
		
		public DisconnectedTile(ILogisticsNetwork network, INetworkTile tile){
			super(network);
			this.tile = tile;
		}
		
	}
	
	public static class ConnectedLocalProvider extends NetworkEvent{
				
		public final IInfoProvider tile;
		
		public ConnectedLocalProvider(ILogisticsNetwork network, IInfoProvider tile){
			super(network);
			this.tile = tile;
		}
		
	}
	
	public static class DisconnectedLocalProvider extends NetworkEvent{
				
		public final IInfoProvider tile;
		
		public DisconnectedLocalProvider(ILogisticsNetwork network, IInfoProvider tile){
			super(network);
			this.tile = tile;
		}
		
	}
	
}
