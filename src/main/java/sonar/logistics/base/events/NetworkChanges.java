package sonar.logistics.base.events;

import sonar.logistics.core.tiles.connections.data.network.LogisticsNetwork;

import java.util.List;

/**it is recommended to always use the LOCAL changes, as these will trigger global changes anyway.*/
public enum NetworkChanges {
	
	LOCAL_PROVIDERS(networks -> LogisticsEventHandler.instance().updateLocalProviders(networks)),
	LOCAL_CHANNELS(networks -> LogisticsEventHandler.instance().updateLocalChannels(networks)),
	GLOBAL_PROVIDERS(networks -> LogisticsEventHandler.instance().updateGlobalProviders(networks)),
	GLOBAL_CHANNELS(networks -> LogisticsEventHandler.instance().updateGlobalChannels(networks));
	
	public final IUpdateNetwork network;
	
	NetworkChanges(IUpdateNetwork network){
		this.network = network;
	}
	
	public interface IUpdateNetwork{
		
		void performUpdates(List<LogisticsNetwork> networks);
		
	}
	
}
