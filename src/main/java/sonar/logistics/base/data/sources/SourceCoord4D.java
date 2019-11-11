package sonar.logistics.base.data.sources;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class SourceCoord4D implements IDataSource {

    public int x, y, z, dimension;
    public EnumFacing facing;

    public SourceCoord4D(int x, int y, int z, int dimension, EnumFacing facing){
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.facing = facing;
    }

    public int getDimension(){
        return dimension;
    }

    public BlockPos getPos(){
        return new BlockPos(x,y,z);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SourceCoord4D)) {
            return false;
        }
        SourceCoord4D c = (SourceCoord4D) obj;
        return x == c.x && y == c.y && z == c.z && dimension == c.dimension && facing == c.facing;
    }

    @Override
    public int hashCode(){
        return Objects.hash(x, y, z, dimension, facing);
    }

}
