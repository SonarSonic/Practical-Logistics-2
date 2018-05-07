package sonar.logistics.info.paths;

import sonar.logistics.api.info.handlers.ITileInfoProvider;

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