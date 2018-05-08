package sonar.logistics.core.tiles.displays.tiles;

import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.core.tiles.base.TileSidedLogistics;

public abstract class TileAbstractDisplay extends TileSidedLogistics implements IDisplay {

	public SyncTagType.INT displaying_gsi = (INT) new SyncTagType.INT(2).setDefault(-1); // id of the connected display.
	
	{
		syncList.addPart(displaying_gsi);
	}
	
	public IDisplay getActualDisplay() {
		return this;
	}	
	
	@Override
	public boolean maxRender() {
		return true;
	}
}
