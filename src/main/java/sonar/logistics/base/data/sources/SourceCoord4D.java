package sonar.logistics.base.data.sources;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class SourceCoord4D implements IDataSource {

    public int x, y, z, dimension;
    public EnumFacing facing;

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
        return x == c.x && y == c.y && z == c.z && dimension == c.dimension;
    }

}
