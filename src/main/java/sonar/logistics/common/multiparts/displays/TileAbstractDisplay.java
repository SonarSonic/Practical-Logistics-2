package sonar.logistics.common.multiparts.displays;

import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.TileSidedLogistics;

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
