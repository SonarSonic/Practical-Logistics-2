package sonar.logistics.networking.cabling;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.logistics.api.cabling.IRedstoneConnectable;

public interface IRedstoneNetwork extends ISonarListener, ISonarListenable<IRedstoneNetwork> {

	/**networks which provides power to this one*/
	public static final int CONNECTED_NETWORK = 0;
	
	/**networks which take power from this one*/
	public static final int WATCHING_NETWORK = 1;

	public int getNetworkID();
	
	public void markCablesChanged();
	
	public boolean doCablesNeedUpdate();
	
	public void tick();
	
	public int updateActualPower();
	
	public int updateLocalPower();
	
	public int updateGlobalPower();
	
	public int getActualPower();
	
	public int getLocalPower();
	
	public int getGlobalPower();
	
	public void notifyWatchingNetworksOfChange();
	
	public void onNetworkPowerChanged(IRedstoneNetwork network);
	
	public void addConnection(IRedstoneConnectable connectable);

	public void removeConnection(IRedstoneConnectable connectable);

}
