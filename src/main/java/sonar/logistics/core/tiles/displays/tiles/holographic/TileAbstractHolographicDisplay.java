package sonar.logistics.core.tiles.displays.tiles.holographic;

import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.displays.tiles.ISmallDisplay;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.Optional;

public abstract class TileAbstractHolographicDisplay extends TileAbstractDisplay implements ISmallDisplay {

    public static final int DEFAULT_COLOUR = FontHelper.getIntFromColor(50, 50, 100);
    public DisplayGSI gsi;
    public EntityHolographicDisplay entity;
    public SyncTagType.INT screenColour = (SyncTagType.INT) new SyncTagType.INT(3).setDefault(DEFAULT_COLOUR);
    public int holographic_display_entity_id = -1;
    {
        syncList.addPart(screenColour);
    }
    //// IInfoDisplay \\\\

    public Optional<EntityHolographicDisplay> getHolographicEntity(){
        if((entity == null || entity.isDead) && this.identity != -1){
           Entity entity = holographic_display_entity_id== -1 ? null : world.getEntityByID(holographic_display_entity_id);
           if(entity instanceof EntityHolographicDisplay){
               this.entity = (EntityHolographicDisplay) entity;
           }else{
               this.entity = spawnDisplayScreen();
           }
        }
        return Optional.ofNullable(entity);
    }

    public EntityHolographicDisplay spawnDisplayScreen(){
        EntityHolographicDisplay display = new EntityHolographicDisplay(getActualWorld(), this);
        if(getActualWorld().spawnEntity(display)) {
            holographic_display_entity_id = display.getEntityId();
        }
        return display;
    }

    @Override
    public DisplayGSI getGSI() {
        return gsi;
    }

    @Override
    public void setGSI(DisplayGSI gsi) {
        this.gsi = gsi;
    }

    public NBTHelper.SyncType getUpdateTagType(){
        return NBTHelper.SyncType.SAVE;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        if(isClient()){
            this.getHolographicEntity().ifPresent(display -> display.setSizingFromDisplay(this));
        }
    }

    @Override
    public void onGSIValidate(){
        super.onGSIValidate();
        if(this.getActualWorld().isRemote){
            this.getHolographicEntity().ifPresent(display -> display.setSizingFromDisplay(this));
        }
    }

    @Override
    public void onGSIInvalidate(){
        super.onGSIInvalidate();
        if(this.getActualWorld().isRemote){
            this.getHolographicEntity().ifPresent(display -> display.setDead());
        }
    }

    public abstract Vec3d getScreenOffset();

    public Vec3d getScreenOrigin(){
        return new Vec3d(getPos()).addVector(0.5,0.5,0.5).add(getScreenOffset());
    }

    public int getScreenColour(){
        return screenColour.getObject();
    }

    public void setScreenColour(int colour){
        screenColour.setObject(colour);
    }

    public abstract void sendPropertiesToServer();
}
