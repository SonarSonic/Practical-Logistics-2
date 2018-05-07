package sonar.logistics.api.displays.tiles;

/** the layout of a display screen */
public enum DisplayLayout {
	ONE(1), DUAL(2), GRID(4), LIST(4);// , ALL(1);

	public int maxInfo;

	DisplayLayout(int maxInfo) {
		this.maxInfo = maxInfo;
	}
}