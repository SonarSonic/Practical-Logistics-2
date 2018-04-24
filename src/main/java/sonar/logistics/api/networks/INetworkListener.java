package sonar.logistics.api.networks;

import sonar.core.utils.IValidate;
import sonar.core.utils.IWorldTile;

public interface INetworkListener extends IValidate, IWorldTile {

	void onNetworkConnect(ILogisticsNetwork network);
	
	void onNetworkDisconnect(ILogisticsNetwork network);

	int getNetworkID();
	
}
