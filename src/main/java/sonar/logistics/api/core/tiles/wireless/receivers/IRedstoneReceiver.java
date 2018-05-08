package sonar.logistics.api.core.tiles.wireless.receivers;

import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneConnectable;
import sonar.logistics.api.core.tiles.connections.redstone.IWirelessRedstoneTile;
import sonar.logistics.api.core.tiles.wireless.emitters.IRedstoneEmitter;

import java.util.List;

public interface IRedstoneReceiver extends IWirelessReceiver<IRedstoneEmitter>, IWirelessRedstoneTile, IRedstoneConnectable {

	void updatePower();

	void refreshConnectedNetworks();
	
	List<Integer> getConnectedNetworks();
	
	//public void onEmitterPowerChanged(IRedstoneEmitter emitters);
}
