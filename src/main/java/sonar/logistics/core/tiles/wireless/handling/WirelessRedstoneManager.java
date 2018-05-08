package sonar.logistics.core.tiles.wireless.handling;

import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.redstone.network.IRedstoneNetwork;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessConnectionType;
import sonar.logistics.api.core.tiles.wireless.emitters.IDataEmitter;
import sonar.logistics.api.core.tiles.wireless.emitters.IRedstoneEmitter;
import sonar.logistics.api.core.tiles.wireless.receivers.IDataReceiver;
import sonar.logistics.api.core.tiles.wireless.receivers.IRedstoneReceiver;
import sonar.logistics.core.tiles.connections.redstone.handling.RedstoneConnectionHandler;

import java.util.List;

public class WirelessRedstoneManager extends AbstractWirelessManager<IRedstoneNetwork, IRedstoneEmitter, IRedstoneReceiver> {

	public static WirelessRedstoneManager instance() {
		return PL2.proxy.wirelessRedstoneManager;
	}

	@Override
	public EnumWirelessConnectionType type() {
		return EnumWirelessConnectionType.REDSTONE;
	}

	/** connects two {@link IRedstoneNetwork}'s so the {@link IDataReceiver}'s handling can read the {@link IDataEmitter}'s handling
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters handling)
	 * @param connected the Data Emitter's Network (which is connected to by the receivers handling) */
	public void connectNetworks(IRedstoneNetwork watcher, IRedstoneNetwork connected) {
		if (watcher.isValid() && connected.isValid()) {
			watcher.getListenerList().addListener(connected, IRedstoneNetwork.CONNECTED_NETWORK);
			connected.getListenerList().addListener(watcher, IRedstoneNetwork.WATCHING_NETWORK);
			watcher.markCablesChanged();
		}
	}

	/** disconnects two {@link IRedstoneNetwork}'s so the {@link IDataReceiver}'s handling can no longer read the {@link IDataEmitter}'s handling, however if multiple receivers/emitters between the two connections exist the connections will remain connected
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters handling)
	 * @param connected the {@link IDataEmitter}'s Network (which is connected to by the receivers handling) */
	public void disconnectNetworks(IRedstoneNetwork watcher, IRedstoneNetwork connected) {
		if (watcher.isValid() && connected.isValid()) {
			watcher.getListenerList().removeListener(connected, true, IRedstoneNetwork.CONNECTED_NETWORK);
			connected.getListenerList().removeListener(watcher, true, IRedstoneNetwork.WATCHING_NETWORK);
			watcher.markCablesChanged();
		}
	}

	/** connects a {@link IDataReceiver} to a {@link IRedstoneNetwork}'s
	 * @param main the {@link IDataReceiver}'s handling
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
	 * @param network the {@link IDataReceiver}'s handling
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

	/* public void onEmitterPowerChanged(IRedstoneEmitter emitters) { receivers.forEach(receivers -> { if (receivers.canEmitterAccessReceiver(emitters).isConnected()){ //receivers.onEmitterPowerChanged(emitters); } }); dirty = true; // updates packets of viewable emitters } */
}
