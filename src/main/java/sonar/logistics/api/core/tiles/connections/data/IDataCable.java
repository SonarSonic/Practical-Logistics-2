package sonar.logistics.api.core.tiles.connections.data;

import sonar.core.utils.IWorldPosition;
import sonar.core.utils.IWorldTile;
import sonar.logistics.api.core.tiles.connections.ICable;
import sonar.logistics.api.core.tiles.connections.ICableConnectable;
import sonar.logistics.api.core.tiles.connections.ICableRenderer;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;

/** implemented on Tile Entities parts which are connections */
public interface IDataCable extends ICableRenderer, IWorldPosition, IWorldTile, ICable, ICableConnectable {
	
	ILogisticsNetwork getNetwork();
	
	void updateCableRenders();
}
