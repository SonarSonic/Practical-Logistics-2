package sonar.logistics.api.wireless;

import java.util.ArrayList;

import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.RefreshType;

/** implemented on the Data Receiver */
public interface IDataReceiver extends ILogicTile {

	/** gets the network ID of all currently connected networks */
	public ArrayList<Integer> getConnectedNetworks();

	/** rechecks connected Data Emitters to ensure the connected network IDs are correct. Typically triggered by an alert {@link RefreshType} */
	public void refreshConnectedNetworks();

	public static void addConnectedNetworks(IDataReceiver receiver, ArrayList<Integer> networks) {
		ArrayList<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(network -> {
			if (!networks.contains(network)) {
				networks.add(network);
			}
		});
	}
}
