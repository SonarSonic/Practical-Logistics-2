package sonar.logistics.api.core.tiles.wireless.receivers;

import java.util.List;

/** implemented on the Data Receiver */
public interface IDataReceiver extends IWirelessReceiver {

	/** gets the handling ID of all currently connected connections */
    List<Integer> getConnectedNetworks();

	/** rechecks connected Data Emitters to ensure the connected handling IDs are correct. */
    void refreshConnectedNetworks();

}
