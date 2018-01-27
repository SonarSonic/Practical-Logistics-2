package sonar.logistics.api.viewers;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.PlayerListener;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.networking.PL2ListenerList;

public interface ILogicListenable extends INetworkTile, ISonarListenable<PlayerListener> {

	PL2ListenerList getListenerList();
}
