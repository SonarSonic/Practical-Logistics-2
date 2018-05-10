package sonar.logistics.core.tiles.displays.tiles.holographic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import sonar.core.helpers.NBTHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;
import sonar.logistics.network.packets.PacketHolographicDisplayScaling;

public class TileAdvancedHolographicDisplay extends TileAbstractHolographicDisplay {

    public Vec3d screenScale = new Vec3d( 1, 1, 0.001);
    public Vec3d screenRotation = new Vec3d(0,0,0);
    public Vec3d screenOffset = new Vec3d(0,0,0);
    public boolean defaults_set = false;

    @Override
    public void readData(NBTTagCompound nbt, NBTHelper.SyncType type) {
        super.readData(nbt, type);
        if(type.isType(NBTHelper.SyncType.SAVE)) {
            defaults_set = nbt.getBoolean("defs");
            screenScale = DisplayVectorHelper.readVec3d("scale", nbt, type);
            screenRotation = DisplayVectorHelper.readVec3d("rotate", nbt, type);
            screenOffset = DisplayVectorHelper.readVec3d("offset", nbt, type);
            if (isClient() && getWorld() != null) {
                getHolographicEntity().ifPresent(entity -> entity.setSizingFromDisplay(this));
            }
        }
    }

    @Override
    public NBTTagCompound writeData(NBTTagCompound nbt, NBTHelper.SyncType type) {
        if(type.isType(NBTHelper.SyncType.SAVE)) {
            this.updateDefaultScaling();
            nbt.setBoolean("defs", defaults_set);
            DisplayVectorHelper.writeVec3d(screenScale, "scale", nbt, type);
            DisplayVectorHelper.writeVec3d(screenRotation, "rotate", nbt, type);
            DisplayVectorHelper.writeVec3d(screenOffset, "offset", nbt, type);
        }
        return super.writeData(nbt, type);
    }

    public Vec3d getScreenScaling(){
        return screenScale;
    }

    public void setScaling(double width, double height, double depth){
        screenScale = new Vec3d(width, height, depth);
        markDirty();
    }

    public Vec3d getScreenRotation(){
        return screenRotation;
    }

    public void setRotation(double pitch, double yaw, double roll){
        screenRotation = new Vec3d(pitch, yaw, roll);
        markDirty();
    }

    public Vec3d getScreenOffset(){
        return screenOffset;
    }

    public void setScreenOffset(double x, double y, double z){
        screenOffset = new Vec3d(x, y, z);
        markDirty();
    }

    @Override
    public void doAdditionEvent(PL2AdditionType type) {
        super.doAdditionEvent(type);
        updateDefaultScaling();
    }

    public void updateDefaultScaling(){
        if(this.isServer() && !defaults_set) {
            screenOffset = DisplayVectorHelper.getFaceOffset(getCableFace(), 0.5);
            screenRotation = DisplayVectorHelper.getScreenRotation(getCableFace());
            defaults_set = true;
            markDirty();
        }
    }

    public void sendPropertiesToServer(){
        if(isClient()){
            PL2.network.sendToServer(new PacketHolographicDisplayScaling(this));
        }
    }

    @Override
    public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
        return EnumCableRenderSize.HALF;
    }
}
