package sonar.logistics.networking.cabling;

import java.util.List;

import sonar.logistics.PL2;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.api.wireless.WirelessConnectionType;
import sonar.logistics.networking.NetworkUpdate;

public class WirelessDataManager extends AbstractWirelessManager<ILogisticsNetwork, IDataEmitter, IDataReceiver> {

	@Override
	public WirelessConnectionType type() {
		return WirelessConnectionType.DATA;
	}
	
	/** connects two {@link ILogisticsNetwork}'s so the {@link IDataReceiver}'s network can read the {@link IDataEmitter}'s network
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters network)
	 * @param connected the Data Emitter's Network (which is connected to by the receivers network) */
	public void connectNetworks(ILogisticsNetwork watcher, ILogisticsNetwork connected) {
		watcher.getListenerList().addListener(connected, ILogisticsNetwork.CONNECTED_NETWORK);
		connected.getListenerList().addListener(watcher, ILogisticsNetwork.WATCHING_NETWORK);
		watcher.markUpdate(NetworkUpdate.GLOBAL, NetworkUpdate.NOTIFY_WATCHING_NETWORKS);
	}

	/** disconnects two {@link ILogisticsNetwork}'s so the {@link IDataReceiver}'s network can no longer read the {@link IDataEmitter}'s network, however if multiple receivers/emitters between the two networks exist the networks will remain connected
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters network)
	 * @param connected the {@link IDataEmitters}'s Network (which is connected to by the receivers network) */
	public void disconnectNetworks(ILogisticsNetwork watcher, ILogisticsNetwork connected) {
		watcher.getListenerList().removeListener(connected, true, ILogisticsNetwork.CONNECTED_NETWORK);
		connected.getListenerList().removeListener(watcher, true, ILogisticsNetwork.WATCHING_NETWORK);
		watcher.markUpdate(NetworkUpdate.GLOBAL, NetworkUpdate.NOTIFY_WATCHING_NETWORKS);
	}

	/** connects a {@link IDataReceiver} to a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataReceiver}'s network
	 * @param receiver the {@link IDataReceiver} which has been connected */
	public void onReceiverConnected(ILogisticsNetwork main, IDataReceiver receiver) {
		receiver.refreshConnectedNetworks();
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			ILogisticsNetwork sub = PL2.getNetworkManager().getNetwork(networkID);
			if (sub.getNetworkID() != main.getNetworkID() && sub.isValid()) {
				connectNetworks(main, sub);
			}
		});
	}

	/** disconnects a {@link IDataReceiver} from a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataReceiver}'s network
	 * @param receiver the {@link IDataReceiver} which has been disconnected */
	public void onReceiverDisconnected(ILogisticsNetwork network, IDataReceiver receiver) {
		receiver.refreshConnectedNetworks();
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			ILogisticsNetwork sub = PL2.getNetworkManager().getNetwork(networkID);
			if (sub.getNetworkID() != network.getNetworkID() && sub.isValid()) {
				disconnectNetworks(network, sub);
			}
		});
	}

}