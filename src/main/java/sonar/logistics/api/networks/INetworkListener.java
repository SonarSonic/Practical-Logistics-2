package sonar.logistics.api.networks;

import sonar.core.utils.IValidate;
import sonar.core.utils.IWorldTile;

public interface INetworkListener extends IValidate, IWorldTile {

	public void onNetworkConnect(ILogisticsNetwork network);
	
	public void onNetworkDisconnect(ILogisticsNetwork network);

	public int getNetworkID();
	
}
