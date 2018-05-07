package sonar.logistics.common.multiparts.holographic;

import net.minecraft.util.math.Vec3d;
import sonar.logistics.api.displays.tiles.DisplayType;

public class TileHolographicDisplay extends TileAbstractHolographicDisplay {

    @Override
    public Vec3d getScreenScaling() {
        return new Vec3d(1-(0.0625*2),1-(0.0625*2),0.001);
    }

    @Override
    public Vec3d getScreenRotation() {
        return HolographicVectorHelper.getScreenRotation(getCableFace());
    }

    @Override
    public Vec3d getScreenOffset() {
        double x = 0;
        double y = getCableFace().getAxis().isHorizontal() ? 0.0625*10 : 0;
        double z = getCableFace().getAxis().isVertical() ? 0.0625*10 : 0;

        return new Vec3d(x, y, z).add(HolographicVectorHelper.getScreenOffset(getCableFace()));
    }

    @Override
    public DisplayType getDisplayType() {
        return DisplayType.HOLOGRAPHIC;
    }
}
