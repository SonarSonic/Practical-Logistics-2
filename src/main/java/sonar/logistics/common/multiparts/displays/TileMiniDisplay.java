package sonar.logistics.common.multiparts.displays;

import sonar.logistics.api.displays.tiles.DisplayType;

public class TileMiniDisplay extends TileDisplayScreen {

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.MINI_DISPLAY;
	}
}
