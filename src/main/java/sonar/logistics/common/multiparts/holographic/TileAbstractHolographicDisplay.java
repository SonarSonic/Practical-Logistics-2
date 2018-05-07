package sonar.logistics.common.multiparts.holographic;

import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.tiles.IScaleableDisplay;
import sonar.logistics.api.displays.tiles.ISmallDisplay;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.api.utils.PL2RemovalType;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

public abstract class TileAbstractHolographicDisplay extends TileAbstractDisplay implements ISmallDisplay, IScaleableDisplay {

    public static final int DEFAULT_COLOUR = FontHelper.getIntFromColor(50, 50, 100);
    public DisplayGSI gsi;
    public EntityHolographicDisplay entity;
    public SyncTagType.INT screenColour = (SyncTagType.INT) new SyncTagType.INT(3).setDefault(DEFAULT_COLOUR);
    public int holographic_display_entity_id = -1;
    {
        syncList.addPart(screenColour);
    }
    //// IInfoDisplay \\\\

    public EntityHolographicDisplay getHolographicEntity(){
        if(entity == null || entity.isDead){
           Entity entity = holographic_display_entity_id== -1 ? null : world.getEntityByID(holographic_display_entity_id);
           if(entity instanceof EntityHolographicDisplay){
               this.entity = (EntityHolographicDisplay) entity;
           }else{
               this.entity = spawnDisplayScreen();
           }
        }
        return entity;
    }

    public EntityHolographicDisplay spawnDisplayScreen(){
        EntityHolographicDisplay display = new EntityHolographicDisplay(getActualWorld(), this);
        if(getActualWorld().spawnEntity(display)) {
            holographic_display_entity_id = display.getEntityId();
        }
        return display;
    }

    @Override
    public void doAdditionEvent(PL2AdditionType type) {
        super.doAdditionEvent(type);
        if(this.getActualWorld().isRemote){
            this.getHolographicEntity();
        }
    }

    @Override
    public void doRemovalEvent(PL2RemovalType type) {
        super.doRemovalEvent(type);
        if(this.getActualWorld().isRemote){
            this.getHolographicEntity().setDead();
        }
    }

    @Override
    public void validate() {
        super.validate();
        if(this.isClient()){
            getHolographicEntity();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(this.getActualWorld().isRemote){
            this.getHolographicEntity().setDead();
        }
    }

    @Override
    public DisplayGSI getGSI() {
        return gsi;
    }

    @Override
    public void setGSI(DisplayGSI gsi) {
        this.gsi = gsi;
    }

    @Override
    public int getInfoContainerID() {
        return getIdentity();
    }

    @Override
    public CableRenderType getCableRenderSize(EnumFacing dir) {
        return CableRenderType.INTERNAL;
    }

    public NBTHelper.SyncType getUpdateTagType(){
        return NBTHelper.SyncType.SAVE;
    }


    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        if(isClient()){
            getHolographicEntity().setSizingFromDisplay(this);
        }
    }

    @Override
    public double[] getScaling() {
        return new double[]{this.getWidth(), this.getHeight(), 100};
    }

    /**width/height depth*/
    public abstract Vec3d getScreenScaling();

    public double getWidth(){
        return getScreenScaling().x;
    }

    public double getHeight(){
        return getScreenScaling().y;
    }

    public double getDepth(){
        return getScreenScaling().z;
    }

    /**pitch / yaw / roll*/
    public abstract Vec3d getScreenRotation();

    public double getPitch(){
        return getScreenRotation().x;
    }

    public double getYaw(){
        return getScreenRotation().y;
    }

    public double getRoll(){
        return getScreenRotation().z;
    }

    public abstract Vec3d getScreenOffset();

    public Vec3d getScreenPosition(){
        return new Vec3d(getPos()).add(getScreenOffset());
    }

    public int getScreenColour(){
        return screenColour.getObject();
    }

    public void setScreenColour(int colour){
        screenColour.setObject(colour);
    }
}
