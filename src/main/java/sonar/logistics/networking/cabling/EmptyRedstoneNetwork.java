package sonar.logistics.networking.cabling;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.logistics.api.cabling.IRedstoneConnectable;
import sonar.logistics.api.networking.IRedstoneNetwork;

public class EmptyRedstoneNetwork implements IRedstoneNetwork {

	public static final EmptyRedstoneNetwork INSTANCE = new EmptyRedstoneNetwork();
	
	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public ListenableList<IRedstoneNetwork> getListenerList() {
		return null;
	}

	@Override
	public void onListenerAdded(ListenerTally<IRedstoneNetwork> tally) {}

	@Override
	public void onListenerRemoved(ListenerTally<IRedstoneNetwork> tally) {}

	@Override
	public void onSubListenableAdded(ISonarListenable<IRedstoneNetwork> listen) {}

	@Override
	public void onSubListenableRemoved(ISonarListenable<IRedstoneNetwork> listen) {	}

	@Override
	public void markCablesChanged() {}

	@Override
	public boolean doCablesNeedUpdate() {
		return false;
	}

	@Override
	public void tick() {
		
	}

	@Override
	public int updateActualPower() {
		return 0;
	}

	@Override
	public void onNetworkPowerChanged(IRedstoneNetwork network) {}

	@Override
	public void addConnection(IRedstoneConnectable connectable) {}

	@Override
	public void removeConnection(IRedstoneConnectable connectable) {}

	@Override
	public int getNetworkID() {
		return -1;
	}

	@Override
	public int updateLocalPower() {
		return 0;
	}

	@Override
	public int updateGlobalPower() {
		return 0;
	}

	@Override
	public int getActualPower() {
		return 0;
	}

	@Override
	public int getLocalPower() {
		return 0;
	}

	@Override
	public int getGlobalPower() {
		return 0;
	}

	@Override
	public void notifyWatchingNetworksOfChange() {}

}
