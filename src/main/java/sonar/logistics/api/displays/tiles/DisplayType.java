package sonar.logistics.api.displays.tiles;

/** the various types of Display Screen */
public enum DisplayType {

	SMALL(0.0625, 0.0625 * 5, 0.0625 * 6, 0.0625 * 14), //
	MINI_DISPLAY(0.0625*5, 0.0625 * 5, 0.0625 * 6, 0.0625 * 6), //
	HOLOGRAPHIC(0.0625, 0.0625, 0.0625 * 6, 0.0625 * 14), //
	LARGE(0.0625, 0.0625, 0.0625 * 14, 0.0625 * 14), //
	ENTITY_HOLOGRAPHIC(0.0625, 0.0625, 0.0625 * 14, 0.0625 * 14), //
	CONNECTED(0.0625, 0.0625, 0.0625 * 16, 0.0625 * 16);//
	public double width, height;
	public double xPos, yPos;

	DisplayType(double xPos, double yPos, double height, double width) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.height = height;
		this.width = width;
	}

	public int getInfoMax() {
		if (this == SMALL || this == HOLOGRAPHIC) {
			return 2;
		}
		return 16;
	}

	public boolean isStaticScreen() {
		return this != CONNECTED;
	}

	public boolean newMethod() {
		return this != CONNECTED;
	}
}
