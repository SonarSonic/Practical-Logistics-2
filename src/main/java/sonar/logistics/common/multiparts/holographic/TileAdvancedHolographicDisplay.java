package sonar.logistics.common.multiparts.holographic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import sonar.core.helpers.NBTHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.displays.tiles.DisplayType;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.packets.PacketHolographicDisplayScaling;

public class TileAdvancedHolographicDisplay extends TileAbstractHolographicDisplay {

    public Vec3d screenScale = new Vec3d( 1, 1, 0.001);
    public Vec3d screenRotation = new Vec3d(0,0,0);
    public Vec3d screenOffset = new Vec3d(0,0,0);
    public boolean defaults_set = false;

    public static Vec3d readVec3d(String tagName, NBTTagCompound nbt, NBTHelper.SyncType type) {
        NBTTagCompound vecTag = nbt.getCompoundTag(tagName);
        return new Vec3d(vecTag.getDouble("x"), vecTag.getDouble("y"), vecTag.getDouble("z"));
    }

    public static NBTTagCompound writeVec3d(Vec3d vec, String tagName, NBTTagCompound nbt, NBTHelper.SyncType type){
        NBTTagCompound vecTag = new NBTTagCompound();
        vecTag.setDouble("x", vec.x);
        vecTag.setDouble("y", vec.y);
        vecTag.setDouble("z", vec.z);
        nbt.setTag(tagName, vecTag);
        return nbt;
    }

    @Override
    public void readData(NBTTagCompound nbt, NBTHelper.SyncType type) {
        super.readData(nbt, type);
        if(type.isType(NBTHelper.SyncType.SAVE)) {
            defaults_set = nbt.getBoolean("defs");
            screenScale = readVec3d("scale", nbt, type);
            screenRotation = readVec3d("rotate", nbt, type);
            screenOffset = readVec3d("offset", nbt, type);
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
            writeVec3d(screenScale, "scale", nbt, type);
            writeVec3d(screenRotation, "rotate", nbt, type);
            writeVec3d(screenOffset, "offset", nbt, type);
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
            screenOffset = HolographicVectorHelper.getScreenOffset(getCableFace());
            screenRotation = HolographicVectorHelper.getScreenRotation(getCableFace());
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
    public DisplayType getDisplayType() {
        return DisplayType.ENTITY_HOLOGRAPHIC;
    }

    @Override
    public CableRenderType getCableRenderSize(EnumFacing dir) {
        return CableRenderType.HALF;
    }
}
