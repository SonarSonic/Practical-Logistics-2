package sonar.logistics.core.tiles.displays.info.paths;

import sonar.logistics.api.core.tiles.displays.info.handlers.ITileInfoProvider;

public class TileHandlerMethod {
	public ITileInfoProvider handler;
	public Integer bitCode = 0;

	public TileHandlerMethod(ITileInfoProvider handler) {
		this.handler = handler;
	}

	public void incrementBit() {
		bitCode++;
	}
}