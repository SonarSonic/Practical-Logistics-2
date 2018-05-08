package sonar.logistics.api.core.tiles.connections.redstone;

import sonar.core.utils.IValidate;
import sonar.logistics.api.core.tiles.connections.redstone.network.IRedstoneNetwork;

public interface IRedstoneListener extends IValidate {

	void onNetworkConnect(IRedstoneNetwork network);
	
	void onNetworkDisconnect(IRedstoneNetwork network);

	int getNetworkID();
	
}	
