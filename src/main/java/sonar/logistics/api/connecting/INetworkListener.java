package sonar.logistics.api.connecting;

import sonar.core.utils.IValidate;
import sonar.logistics.api.cabling.ILogicTile;

public interface INetworkListener extends IValidate {

	public void onNetworkConnect(ILogisticsNetwork network);
	
	public void onNetworkDisconnect(ILogisticsNetwork network);

	public int getNetworkID();
	
}
