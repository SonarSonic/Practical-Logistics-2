package sonar.logistics.core.tiles.displays.gsi.interaction;

public class DisplayScreenLook {

	public double lookX, lookY;
	public int identity;

	public DisplayScreenLook(){}

	public DisplayScreenLook(double[] lookPosition, int identity){
		this.lookX = lookPosition[0];
		this.lookY = lookPosition[1];
		this.identity = identity;
	}

	public DisplayScreenLook setLookPosition(double[] lookPosition) {
		this.lookX = lookPosition[0];
		this.lookY = lookPosition[1];
		return this;
	}

	public DisplayScreenLook setContainerIdentity(int identity) {
		this.identity = identity;
		return this;
	}

}
