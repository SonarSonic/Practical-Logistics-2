package sonar.logistics.core.tiles.displays.tiles.holographic;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import sonar.core.helpers.NBTHelper;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;

public class TileHolographicDisplay extends TileAbstractHolographicDisplay implements IByteBufTile {

    public static final Vec3d SCREEN_SCALE = new Vec3d(0.0625 * 14, 0.0625 * 14, 0.001);

    @Override
    public Vec3d getScreenScaling() {
        return SCREEN_SCALE;
    }

    @Override
    public Vec3d getScreenRotation() {
        return DisplayVectorHelper.getScreenRotation(getCableFace());
    }

    @Override
    public Vec3d getScreenOffset() {
        double x = 0;
        double y = getCableFace().getAxis().isHorizontal() ? 0.0625*10 : 0;
        double z = getCableFace().getAxis().isVertical() ? 0.0625*10 : 0;

        return new Vec3d(x, y, z).add(DisplayVectorHelper.getFaceOffset(getCableFace(), 0.5));
    }

    @Override
    public void sendPropertiesToServer() {
        this.sendByteBufPacket(100);
    }

    @Override
    public void writePacket(ByteBuf buf, int id){
        if(id==100){
            buf.writeInt(getScreenColour());
        }
    }

    @Override
    public void readPacket(ByteBuf buf, int id){
        if(id==100){
            screenColour.setObject(buf.readInt());
            getGSI().getWatchers().forEach(watcher -> sendSyncPacket(watcher, NBTHelper.SyncType.SAVE));
        }
    }
}
