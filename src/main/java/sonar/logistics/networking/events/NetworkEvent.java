package sonar.logistics.networking.events;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.logistics.api.networks.ILogisticsNetwork;

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
	
}
