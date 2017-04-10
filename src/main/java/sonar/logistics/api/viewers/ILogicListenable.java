package sonar.logistics.api.viewers;

import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.logistics.api.tiles.INetworkTile;

public interface ILogicListenable<L extends ISonarListener> extends INetworkTile, ISonarListenable<L> {
				
}
