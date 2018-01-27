package sonar.logistics.api.wireless;

import java.util.List;

import sonar.logistics.api.cabling.IRedstoneConnectable;

public interface IRedstoneReceiver extends IWirelessReceiver<IRedstoneEmitter>, IWirelessRedstoneTile, IRedstoneConnectable {

	public void updatePower();

	public void refreshConnectedNetworks();
	
	public List<Integer> getConnectedNetworks();
	
	//public void onEmitterPowerChanged(IRedstoneEmitter emitter);
}
