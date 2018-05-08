package sonar.logistics.core.tiles.displays.tiles.holographic;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.events.PL2Events;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenLook;
import sonar.logistics.core.tiles.displays.gsi.render.GSIOverlays;

public class EntityHolographicDisplay extends Entity {

    public TileAbstractHolographicDisplay display;
    public float rotationRoll;
    public int GSI_IDENTITY;

    public EntityHolographicDisplay(World world) {
        super(world);
    }

    public EntityHolographicDisplay(World world, TileAbstractHolographicDisplay display){
        this(world);
        this.display = display;
        this.GSI_IDENTITY = display.getInfoContainerID();
        setSizingFromDisplay(display);
    }

    public void setSizingFromDisplay(TileAbstractHolographicDisplay display){
        this.display = display;
        Vec3d screenPosition = display.getScreenPosition();
        setPosition(screenPosition.x+0.5, screenPosition.y+0.5, screenPosition.z + 0.5);
        rotationYaw = (float)display.getYaw();
        rotationPitch = (float)display.getPitch();
        rotationRoll = (float)display.getRoll();
        setSize((float)display.getWidth(), (float)display.getHeight());
        createEffectiveBoundingBox();
    }

    public void createEffectiveBoundingBox(){

        double xMin = posX, xMax = posX;
        double yMin = posY, yMax = posY;
        double zMin = posZ, zMax = posZ;
        Vec3d screenV = getLookVec();
        Vec3d origin = getPositionVector();
        Vec3d[] vectors = HolographicVectorHelper.getScreenVectors(this, screenV);

        Vec3d topLeft = HolographicVectorHelper.getTopLeft(origin, vectors[0], vectors[1], width, height);
        Vec3d topRight = HolographicVectorHelper.getTopRight(origin, vectors[0], vectors[1], width, height);
        Vec3d bottomLeft = HolographicVectorHelper.getBottomLeft(origin, vectors[0], vectors[1], width, height);
        Vec3d bottomRight = HolographicVectorHelper.getBottomRight(origin, vectors[0], vectors[1], width, height);

        for(Vec3d vec : Lists.newArrayList(topLeft, topRight, bottomLeft, bottomRight)){
            xMin = vec.x < xMin ? vec.x : xMin;
            yMin = vec.y < yMin ? vec.y : yMin;
            zMin = vec.z < zMin ? vec.z : zMin;

            xMax = vec.x > xMax ? vec.x : xMax;
            yMax = vec.y > yMax ? vec.y : yMax;
            zMax = vec.z > zMax ? vec.z : zMax;
        }
        setEntityBoundingBox(new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax));

        //setEntityBoundingBox(TileEntity.INFINITE_EXTENT_AABB);
    }
    //// ENTITY METHODS \\\\

    @Override
    protected void entityInit() {
        this.isImmuneToFire = true;
        this.setNoGravity(true);
    }

    protected void setSize(float width, float height){
        super.setSize(width, height);
        if(getHolographicDisplay()!=null && getHolographicDisplay().getGSI()!=null)
            getHolographicDisplay().getGSI().updateScaling();
    }
    @Override
    public void onKillCommand(){}

    @Override
    protected boolean canBeRidden(Entity entityIn){
        return false;
    }

    @Override
    public boolean isPushedByWater(){
        return false;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }


    @Override
    public boolean shouldRenderInPass(int pass){
        return pass == 0;
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        return doGSIInteraction(player, player.isSneaking() ? BlockInteractionType.SHIFT_RIGHT : BlockInteractionType.RIGHT, hand) ? EnumActionResult.SUCCESS  : EnumActionResult.PASS;
    }

    public boolean doGSIInteraction(EntityPlayer player, BlockInteractionType type, EnumHand hand){
        DisplayScreenLook look = GSIOverlays.getCurrentLook(GSI_IDENTITY);
        if(look != null) {
            TileAbstractHolographicDisplay display = getHolographicDisplay();
            if (display != null && display.getGSI() != null && PL2Events.coolDownClick == 0) {
                if (!display.isValid()) {
                    this.setDead();
                } else {
                    PL2Events.coolDownClick = 3;
                    display.getGSI().onClicked(display, type, this.getEntityWorld(), display.getPos(), display.getWorld().getBlockState(display.getPos()), player, hand, EnumFacing.NORTH, (float) look.lookX, (float) look.lookY, 0F);
                }
            }
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canBeCollidedWith()
    {
        if(this.world.isRemote){
            double[] look = HolographicVectorHelper.getDisplayLook(FMLClientHandler.instance().getClientPlayerEntity(), this, 8);
            if(look != null){
                GSIOverlays.currentLook = new DisplayScreenLook().setLookPosition(look).setContainerIdentity(GSI_IDENTITY);
                return true;
            }else if(GSIOverlays.getCurrentLook(GSI_IDENTITY) != null){
                GSIOverlays.currentLook = null;
            }
        }
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        GSI_IDENTITY = compound.getInteger("gsi_identity");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("gsi_identity", GSI_IDENTITY);
    }

    //// GSI VALIDATE/INVALIDATE \\\\

    public TileAbstractHolographicDisplay getHolographicDisplay(){
        if(display == null || !display.isValid()){
            if(this.getEntityWorld().isRemote){
                IDisplay display = ClientInfoHandler.instance().displays_tile.get(this.GSI_IDENTITY);
                if(display instanceof TileAbstractHolographicDisplay) {
                    this.display = (TileAbstractHolographicDisplay)display;
                    this.setSizingFromDisplay(this.display);
                }else{
                    this.setDead();
                }
            }
        }
        return display;
    }

}
