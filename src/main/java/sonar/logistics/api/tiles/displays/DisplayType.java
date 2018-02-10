package sonar.logistics.api.tiles.displays;

/**the various types of Display Screen*/
public enum DisplayType {

	SMALL(0.0625 * 6, 0.0625 * 14, 0.008), //
	HOLOGRAPHIC(0.0625 * 6, 0.0625 * 14, 0.008), //
	LARGE(0.0625 * 14, 0.0625 * 14, 0.008), 
	CONNECTED(0.0625 * 16, 0.0625 * 16, 0.008);//
	public double width, height, scale;

	DisplayType(double height, double width, double scale) {
		this.height = height;
		this.width = width;
		this.scale = scale;
	}
	
	public int getInfoMax(){
		if(this == SMALL || this == HOLOGRAPHIC){
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
