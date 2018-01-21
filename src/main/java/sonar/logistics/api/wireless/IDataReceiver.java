package sonar.logistics.api.wireless;

import java.util.List;
import java.util.UUID;

import sonar.logistics.api.tiles.INetworkTile;

/** implemented on the Data Receiver */
public interface IDataReceiver extends IWirelessReceiver {

	/** gets the network ID of all currently connected networks */
	public List<Integer> getConnectedNetworks();

	/** rechecks connected Data Emitters to ensure the connected network IDs are correct. Typically triggered by an alert {@link RefreshType} */
	public void refreshConnectedNetworks();

}
