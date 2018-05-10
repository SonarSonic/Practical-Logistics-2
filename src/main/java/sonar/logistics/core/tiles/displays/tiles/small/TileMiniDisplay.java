package sonar.logistics.core.tiles.displays.tiles.small;

import net.minecraft.util.math.Vec3d;

public class TileMiniDisplay extends TileDisplayScreen {

    public static final Vec3d SCREEN_SCALE = new Vec3d(0.0625 * 6, 0.0625 * 6, 0.001);

    @Override
    public Vec3d getScreenScaling() {
        return SCREEN_SCALE;
    }
}
