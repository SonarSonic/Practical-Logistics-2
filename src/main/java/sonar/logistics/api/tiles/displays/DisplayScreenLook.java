package sonar.logistics.api.tiles.displays;

import net.minecraft.util.math.BlockPos;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.api.displays.InfoContainer;

public class DisplayScreenLook {

	public double lookX, lookY;
	public int identity;

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
