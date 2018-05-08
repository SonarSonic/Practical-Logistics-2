package sonar.logistics.api.core.tiles.displays.tiles;

/** the various types of Display Screen */
public enum EnumDisplayType {

	SMALL(0.0625, 0.0625 * 5, 0.0625 * 6, 0.0625 * 14), //
	MINI_DISPLAY(0.0625*5, 0.0625 * 5, 0.0625 * 6, 0.0625 * 6), //
	HOLOGRAPHIC(0.0625, 0.0625, 0.0625 * 6, 0.0625 * 14), //
	LARGE(0.0625, 0.0625, 0.0625 * 14, 0.0625 * 14), //
	ENTITY_HOLOGRAPHIC(0.0625, 0.0625, 0.0625 * 14, 0.0625 * 14), //
	CONNECTED(0.0625, 0.0625, 0.0625 * 16, 0.0625 * 16);//
	public double width, height;
	public double xPos, yPos;

	EnumDisplayType(double xPos, double yPos, double height, double width) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.height = height;
		this.width = width;
	}
}
