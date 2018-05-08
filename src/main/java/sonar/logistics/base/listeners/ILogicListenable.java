package sonar.logistics.base.listeners;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.PlayerListener;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.base.tiles.INetworkTile;

public interface ILogicListenable extends INetworkTile, ISonarListenable<PlayerListener> {

	PL2ListenerList getListenerList();

	default ILogicListSorter getSorter() {
		return null;
	}
}
