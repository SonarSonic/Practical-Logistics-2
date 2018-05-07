package sonar.logistics.api.cabling;

import sonar.core.utils.IWorldPosition;
import sonar.core.utils.IWorldTile;
import sonar.logistics.api.networking.ILogisticsNetwork;

/** implemented on Tile Entities and Forge Multipart parts which are cables */
public interface IDataCable extends ICableRenderer, IWorldPosition, IWorldTile, ICable, ICableConnectable {
	
	ILogisticsNetwork getNetwork();
	
	void updateCableRenders();
}
