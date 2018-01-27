package sonar.logistics.api.cabling;

import sonar.core.utils.IValidate;
import sonar.logistics.networking.cabling.IRedstoneNetwork;

public interface IRedstoneListener extends IValidate {

	public void onNetworkConnect(IRedstoneNetwork network);
	
	public void onNetworkDisconnect(IRedstoneNetwork network);

	public int getNetworkID();
	
}	
