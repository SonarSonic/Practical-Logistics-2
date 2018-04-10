package sonar.logistics.networking.cabling;

import java.util.List;

import sonar.logistics.PL2;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.api.wireless.IRedstoneEmitter;
import sonar.logistics.api.wireless.IRedstoneReceiver;
import sonar.logistics.api.wireless.WirelessConnectionType;

public class WirelessRedstoneManager extends AbstractWirelessManager<IRedstoneNetwork, IRedstoneEmitter, IRedstoneReceiver> {

	public static WirelessRedstoneManager instance() {
		return PL2.instance.proxy.wirelessRedstoneManager;
	}

	@Override
	public WirelessConnectionType type() {
		return WirelessConnectionType.REDSTONE;
	}

	/** connects two {@link IRedstoneNetwork}'s so the {@link IDataReceiver}'s network can read the {@link IDataEmitter}'s network
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters network)
	 * @param connected the Data Emitter's Network (which is connected to by the receivers network) */
	public void connectNetworks(IRedstoneNetwork watcher, IRedstoneNetwork connected) {
		if (watcher.isValid() && connected.isValid()) {
			watcher.getListenerList().addListener(connected, IRedstoneNetwork.CONNECTED_NETWORK);
			connected.getListenerList().addListener(watcher, IRedstoneNetwork.WATCHING_NETWORK);
			watcher.markCablesChanged();
		}
	}

	/** disconnects two {@link IRedstoneNetwork}'s so the {@link IDataReceiver}'s network can no longer read the {@link IDataEmitter}'s network, however if multiple receivers/emitters between the two networks exist the networks will remain connected
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters network)
	 * @param connected the {@link IDataEmitters}'s Network (which is connected to by the receivers network) */
	public void disconnectNetworks(IRedstoneNetwork watcher, IRedstoneNetwork connected) {
		if (watcher.isValid() && connected.isValid()) {
			watcher.getListenerList().removeListener(connected, true, IRedstoneNetwork.CONNECTED_NETWORK);
			connected.getListenerList().removeListener(watcher, true, IRedstoneNetwork.WATCHING_NETWORK);
			watcher.markCablesChanged();
		}
	}

	/** connects a {@link IDataReceiver} to a {@link IRedstoneNetwork}'s
	 * @param main the {@link IDataReceiver}'s network
	 * @param receiver the {@link IDataReceiver} which has been connected */
	public void onReceiverConnected(IRedstoneNetwork main, IRedstoneReceiver receiver) {
		receiver.refreshConnectedNetworks();
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			IRedstoneNetwork sub = RedstoneConnectionHandler.instance().getNetwork(networkID);
			if (sub.getNetworkID() != main.getNetworkID() && sub.isValid()) {
				connectNetworks(main, sub);
			}
		});
	}

	/** disconnects a {@link IDataReceiver} from a {@link IRedstoneNetwork}'s
	 * @param main the {@link IDataReceiver}'s network
	 * @param receiver the {@link IDataReceiver} which has been disconnected */
	public void onReceiverDisconnected(IRedstoneNetwork network, IRedstoneReceiver receiver) {
		receiver.refreshConnectedNetworks();
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			IRedstoneNetwork sub = RedstoneConnectionHandler.instance().getNetwork(networkID);
			if (sub.getNetworkID() != network.getNetworkID() && sub.isValid()) {
				disconnectNetworks(network, sub);
			}
		});
	}

	/* public void onEmitterPowerChanged(IRedstoneEmitter emitter) { receivers.forEach(receiver -> { if (receiver.canEmitterAccessReceiver(emitter).isConnected()){ //receiver.onEmitterPowerChanged(emitter); } }); dirty = true; // updates packets of viewable emitters } */
}
