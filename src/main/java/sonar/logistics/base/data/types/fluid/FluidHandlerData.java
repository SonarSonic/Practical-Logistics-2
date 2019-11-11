package sonar.logistics.base.data.types.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import sonar.logistics.base.data.api.IData;

import javax.annotation.Nullable;
import java.util.HashMap;

public class FluidHandlerData implements IData {

    public HashMap<Integer, SimpleFluidTank> tankMap = new HashMap<>();
    public boolean hasUpdated = false;

    public void preUpdate(){
        hasUpdated = false;
    }

    public boolean hasUpdated(){
        return hasUpdated;
    }


    public static class SimpleFluidTank{

        @Nullable
        FluidStack contents;
        long capacity;

        public SimpleFluidTank(IFluidTankProperties properties){
            this(properties.getContents() != null ? properties.getContents().copy() : null, properties.getCapacity());
        }

        public SimpleFluidTank(FluidStack contents, long capacity){
            this.contents = contents;
            this.capacity = capacity;
        }

    }

}
