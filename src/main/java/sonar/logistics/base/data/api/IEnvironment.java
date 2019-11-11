package sonar.logistics.base.data.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEnvironment {

    World world();

    IBlockState state();

    BlockPos pos();

    EnumFacing face();

    TileEntity tile();

    default Entity entity(){
        return null; //FIXME ENTITY SYSTEM
    }

}
