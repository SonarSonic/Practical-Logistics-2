package sonar.logistics.api.cabling;

import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.render.ICableRenderer;

/** implemented on Tile Entities and Forge Multipart parts which are cables */
public interface IDataCable extends ICableRenderer, IWorldPosition, ICable, ICableConnectable {
	
	ILogisticsNetwork getNetwork();
	
	void updateCableRenders();
}
