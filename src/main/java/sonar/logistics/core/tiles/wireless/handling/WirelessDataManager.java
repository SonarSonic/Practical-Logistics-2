package sonar.logistics.core.tiles.wireless.handling;

import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessConnectionType;
import sonar.logistics.api.core.tiles.wireless.emitters.IDataEmitter;
import sonar.logistics.api.core.tiles.wireless.receivers.IDataReceiver;
import sonar.logistics.base.events.LogisticsEventHandler;
import sonar.logistics.base.events.NetworkChanges;
import sonar.logistics.base.events.types.NetworkEvent;
import sonar.logistics.core.tiles.connections.data.network.LogisticsNetworkHandler;

import java.util.List;

public class WirelessDataManager extends AbstractWirelessManager<ILogisticsNetwork, IDataEmitter, IDataReceiver> {

	public static WirelessDataManager instance() {
		return PL2.proxy.wirelessDataManager;
	}

	@Override
	public EnumWirelessConnectionType type() {
		return EnumWirelessConnectionType.DATA;
	}
	
	/** connects two {@link ILogisticsNetwork}'s so the {@link IDataReceiver}'s handling can read the {@link IDataEmitter}'s handling
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters handling)
	 * @param connected the Data Emitter's Network (which is connected to by the receivers handling) */
	public void connectNetworks(ILogisticsNetwork watcher, ILogisticsNetwork connected) {
		watcher.getListenerList().addListener(connected, ILogisticsNetwork.CONNECTED_NETWORK);
		connected.getListenerList().addListener(watcher, ILogisticsNetwork.WATCHING_NETWORK);

		LogisticsEventHandler.instance().queueNetworkChange(watcher, NetworkChanges.LOCAL_CHANNELS, NetworkChanges.LOCAL_PROVIDERS);
		LogisticsEventHandler.instance().queueNetworkChange(connected, NetworkChanges.LOCAL_CHANNELS, NetworkChanges.LOCAL_PROVIDERS);
		LogisticsEventHandler.instance().UPDATING.scheduleEvent(new NetworkEvent.ConnectedNetwork(watcher, connected), 0);
	}

	/** disconnects two {@link ILogisticsNetwork}'s so the {@link IDataReceiver}'s handling can no longer read the {@link IDataEmitter}'s handling, however if multiple receivers/emitters between the two connections exist the connections will remain connected
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters handling)
	 * @param connected the {@link IDataEmitter}'s Network (which is connected to by the receivers handling) */
	public void disconnectNetworks(ILogisticsNetwork watcher, ILogisticsNetwork connected) {
		watcher.getListenerList().removeListener(connected, true, ILogisticsNetwork.CONNECTED_NETWORK);
		connected.getListenerList().removeListener(watcher, true, ILogisticsNetwork.WATCHING_NETWORK);
		LogisticsEventHandler.instance().queueNetworkChange(watcher, NetworkChanges.LOCAL_CHANNELS, NetworkChanges.LOCAL_PROVIDERS);
		LogisticsEventHandler.instance().queueNetworkChange(connected, NetworkChanges.LOCAL_CHANNELS, NetworkChanges.LOCAL_PROVIDERS);
		LogisticsEventHandler.instance().UPDATING.scheduleEvent(new NetworkEvent.DisconnectedNetwork(watcher, connected), 1);
	}

	/** connects a {@link IDataReceiver} to a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataReceiver}'s handling
	 * @param receiver the {@link IDataReceiver} which has been connected */
	public void onReceiverConnected(ILogisticsNetwork main, IDataReceiver receiver) {
		receiver.refreshConnectedNetworks();
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			ILogisticsNetwork sub = LogisticsNetworkHandler.instance().getNetwork(networkID);
			if (sub.getNetworkID() != main.getNetworkID() && sub.isValid()) {
				connectNetworks(main, sub);
			}
		});
	}

	/** disconnects a {@link IDataReceiver} from a {@link ILogisticsNetwork}'s
	 * @param network the {@link IDataReceiver}'s handling
	 * @param receiver the {@link IDataReceiver} which has been disconnected */
	public void onReceiverDisconnected(ILogisticsNetwork network, IDataReceiver receiver) {
		receiver.refreshConnectedNetworks();
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			ILogisticsNetwork sub = LogisticsNetworkHandler.instance().getNetwork(networkID);
			if (sub.getNetworkID() != network.getNetworkID() && sub.isValid()) {
				disconnectNetworks(network, sub);
			}
		});
	}

}