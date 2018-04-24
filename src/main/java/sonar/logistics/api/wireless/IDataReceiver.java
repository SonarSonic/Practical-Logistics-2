package sonar.logistics.api.wireless;

import java.util.List;

/** implemented on the Data Receiver */
public interface IDataReceiver extends IWirelessReceiver {

	/** gets the network ID of all currently connected networks */
    List<Integer> getConnectedNetworks();

	/** rechecks connected Data Emitters to ensure the connected network IDs are correct. */
    void refreshConnectedNetworks();

}
