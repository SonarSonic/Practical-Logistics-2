package sonar.logistics.common.multiparts.displays;

import sonar.logistics.api.tiles.displays.DisplayType;

public class TileMiniDisplay extends TileDisplayScreen {

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.MINI_DISPLAY;
	}
}
