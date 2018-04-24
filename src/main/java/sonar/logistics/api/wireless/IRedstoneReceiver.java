package sonar.logistics.api.wireless;

import java.util.List;

import sonar.logistics.api.cabling.IRedstoneConnectable;

public interface IRedstoneReceiver extends IWirelessReceiver<IRedstoneEmitter>, IWirelessRedstoneTile, IRedstoneConnectable {

	void updatePower();

	void refreshConnectedNetworks();
	
	List<Integer> getConnectedNetworks();
	
	//public void onEmitterPowerChanged(IRedstoneEmitter emitter);
}
