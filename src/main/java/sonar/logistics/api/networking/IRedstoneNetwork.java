package sonar.logistics.api.networking;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.logistics.api.cabling.IRedstoneConnectable;

public interface IRedstoneNetwork extends ISonarListener, ISonarListenable<IRedstoneNetwork> {

	/**networking which provides power to this one*/
    int CONNECTED_NETWORK = 0;
	
	/**networking which take power from this one*/
    int WATCHING_NETWORK = 1;

	int getNetworkID();
	
	void markCablesChanged();
	
	boolean doCablesNeedUpdate();
	
	void tick();
	
	int updateActualPower();
	
	int updateLocalPower();
	
	int updateGlobalPower();
	
	int getActualPower();
	
	int getLocalPower();
	
	int getGlobalPower();
	
	void notifyWatchingNetworksOfChange();
	
	void onNetworkPowerChanged(IRedstoneNetwork network);
	
	void addConnection(IRedstoneConnectable connectable);

	void removeConnection(IRedstoneConnectable connectable);

}
