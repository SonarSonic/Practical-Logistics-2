package sonar.logistics.api.cabling;

import sonar.core.utils.IValidate;
import sonar.logistics.networking.cabling.IRedstoneNetwork;

public interface IRedstoneListener extends IValidate {

	void onNetworkConnect(IRedstoneNetwork network);
	
	void onNetworkDisconnect(IRedstoneNetwork network);

	int getNetworkID();
	
}	
