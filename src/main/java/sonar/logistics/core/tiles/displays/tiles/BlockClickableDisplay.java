package sonar.logistics.core.tiles.displays.tiles;

import mcmultipart.api.container.IPartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.common.block.properties.SonarProperties;
import sonar.core.helpers.RayTraceHelper;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.base.events.PL2Events;

import javax.annotation.Nonnull;

/**for gsiMap which can be left clicked*/
public class BlockClickableDisplay extends BlockAbstractDisplay {


    public BlockClickableDisplay(PL2Multiparts multipart) {
        super(multipart);
    }

    /**will be removed*/
    public boolean canPlayerDestroy(IPartInfo part, EntityPlayer player) {
        boolean canDestroy = canEntityDestroy(part.getState(), part.getActualWorld(), part.getPartPos(), player);
        if (!canDestroy) {
            onBlockClicked(part.getPartWorld(), part.getPartPos(), player);
        }
        return canDestroy;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity){
        if(entity instanceof EntityPlayer){
            RayTraceResult rayResult = RayTraceHelper.getRayTraceEyes((EntityPlayer) entity);
            return rayResult == null || state.getValue(SonarProperties.ORIENTATION).getOpposite() == rayResult.sideHit;
        }
        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        if (!canEntityDestroy(state, world, pos, player)) {
            onBlockClicked(world, pos, player);
            return false;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (PL2Events.coolDownClick == 0) {
            PL2Events.coolDownClick = 2;
            RayTraceResult rayResult = RayTraceHelper.getRayTraceEyes(player);
            TileAbstractDisplay display = (TileAbstractDisplay) world.getTileEntity(pos);
            float hitX = (float) (rayResult.hitVec.x - (double) pos.getX());
            float hitY = (float) (rayResult.hitVec.y - (double) pos.getY());
            float hitZ = (float) (rayResult.hitVec.z - (double) pos.getZ());
            if (display.getGSI() != null) {
                display.getGSI().onClicked(display, player.isSneaking() ? BlockInteractionType.SHIFT_LEFT : BlockInteractionType.LEFT, world, pos, world.getBlockState(pos), player, player.getActiveHand(), display.getCableFace(), hitX, hitY, hitZ);
            }
        }
    }
}
