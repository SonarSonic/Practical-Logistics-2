package sonar.logistics.core.tiles.displays.tiles.small;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.displays.tiles.ISmallDisplay;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

public class TileDisplayScreen extends TileAbstractDisplay implements ISmallDisplay {

	public static final Vec3d SCREEN_SCALE = new Vec3d(0.0625 * 14, 0.0625 * 6, 0.001);

	public DisplayGSI container;

	@Override
	public DisplayGSI getGSI() {
		return container;
	}

	@Override
	public void setGSI(DisplayGSI gsi) {
		this.container = gsi;
	}

	@Override
	public Vec3d getScreenScaling() {
		return SCREEN_SCALE;
	}
}