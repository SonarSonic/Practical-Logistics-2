package sonar.logistics.networking.connections;

import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.api.wireless.IRedstoneEmitter;
import sonar.logistics.api.wireless.IRedstoneReceiver;
import sonar.logistics.api.wireless.WirelessConnectionType;
import sonar.logistics.api.wireless.WirelessSecurity;

public class WirelessRedstoneManager extends AbstractWirelessManager<IRedstoneEmitter, IRedstoneReceiver> {

	@Override
	public WirelessConnectionType type() {
		return WirelessConnectionType.REDSTONE;
	}

	@Override
	public void onReceiverConnected(ILogisticsNetwork main, IRedstoneReceiver receiver) {
		receiver.updatePower();
	}

	@Override
	public void onReceiverDisconnected(ILogisticsNetwork network, IRedstoneReceiver receiver) {
		receiver.updatePower();
	}
	
	public void onEmitterPowerChanged(IRedstoneEmitter emitter) {
		receivers.forEach(receiver -> {
			if (receiver.canEmitterAccessReceiver(emitter).isConnected())
				receiver.onEmitterPowerChanged(emitter);
		});
		dirty = true; // updates packets of viewable emitters
	}
}
