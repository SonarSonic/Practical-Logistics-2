package sonar.logistics.api.networks;

import sonar.core.utils.IValidate;

public interface INetworkListener extends IValidate {

	public void onNetworkConnect(ILogisticsNetwork network);
	
	public void onNetworkDisconnect(ILogisticsNetwork network);

	public int getNetworkID();
	
}
