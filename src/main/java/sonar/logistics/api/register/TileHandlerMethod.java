package sonar.logistics.api.register;

import sonar.logistics.api.info.ICustomTileHandler;

public class TileHandlerMethod {
	public ICustomTileHandler handler;
	public Integer bitCode = 0;

	public TileHandlerMethod(ICustomTileHandler handler) {
		this.handler = handler;
	}

	public void incrementBit() {
		bitCode++;
	}
}