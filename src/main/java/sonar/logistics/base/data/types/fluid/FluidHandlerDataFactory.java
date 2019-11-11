package sonar.logistics.base.data.types.fluid;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.logistics.base.data.api.IDataFactory;

import java.util.Map;

public class FluidHandlerDataFactory implements IDataFactory<FluidHandlerData> {

    public static final String CAPACITY_KEY = "capacity";
    public static final String TANK_ID_KEY = "tankID";
    public static final String TANKS_KEY = "tanks";
    public static final String EMPTY_KEY = "Empty";

    @Override
    public FluidHandlerData create() {
        return new FluidHandlerData();
    }

    @Override
    public void save(FluidHandlerData data, String key, NBTTagCompound tag) {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        data.tankMap.forEach((ID, T) -> list.appendTag(saveTank(ID, T)));
        nbt.setTag(TANKS_KEY, list);
        tag.setTag(key, nbt);
    }

    @Override
    public void read(FluidHandlerData data, String key, NBTTagCompound tag) {
        NBTTagCompound nbt = tag.getCompoundTag(key);
        data.tankMap.clear();
        NBTTagList list = nbt.getTagList(TANKS_KEY, Constants.NBT.TAG_COMPOUND);
        list.forEach(nbtBase -> readTank(data, (NBTTagCompound) nbtBase));
    }

    @Override
    public void saveUpdate(FluidHandlerData data, ByteBuf buf) {
        buf.writeInt(data.tankMap.size());
        for(Map.Entry<Integer, FluidHandlerData.SimpleFluidTank> tank : data.tankMap.entrySet()){
            ByteBufUtils.writeTag(buf, saveTank(tank.getKey(), tank.getValue()));
        }
    }

    @Override
    public void readUpdate(FluidHandlerData data, ByteBuf buf) {
        int size = buf.readInt();
        int current_id = 0;
        while(current_id < size){
            readTank(data, ByteBufUtils.readTag(buf));
            current_id++;
        }
    }

    private NBTTagCompound saveTank(Integer tankID, FluidHandlerData.SimpleFluidTank tank){
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger(TANK_ID_KEY, tankID);
        tag.setLong(CAPACITY_KEY, tank.capacity);

        if (tank.contents != null){
            tank.contents.writeToNBT(tag);
        }else{
            tag.setString(EMPTY_KEY, "");
        }

        return tag;
    }

    public FluidHandlerData.SimpleFluidTank readTank(FluidHandlerData data, NBTTagCompound tag){

        int tankID = tag.getInteger(TANK_ID_KEY);
        long capacity = tag.getLong(CAPACITY_KEY);

        FluidStack contents;
        if (!tag.hasKey(EMPTY_KEY)){
            contents = FluidStack.loadFluidStackFromNBT(tag);
        }else{
            contents = null;
        }

        FluidHandlerData.SimpleFluidTank tank = new FluidHandlerData.SimpleFluidTank(contents, capacity);
        data.tankMap.put(tankID, tank);
        return tank;
    }

    @Override
    public boolean canConvert(Class returnType){
        return returnType == IFluidHandler.class;
    }

    @Override
    public void updateData(FluidHandlerData data, Object obj){
        if(obj instanceof IFluidHandler) {
            int tankID = 0;
            for(IFluidTankProperties tank : ((IFluidHandler) obj).getTankProperties()){
                FluidHandlerData.SimpleFluidTank simpleTank = data.tankMap.get(tankID);
                if(simpleTank != null){
                    if(!equalNullableFluidStacks(tank.getContents(), simpleTank.contents)){
                        simpleTank.contents = tank.getContents();
                        data.hasUpdated = true;
                    }
                }else{
                    data.tankMap.put(tankID, new FluidHandlerData.SimpleFluidTank(tank));
                    data.hasUpdated = true;
                }

                tankID++;
            }
        }
    }



    private boolean equalNullableFluidStacks(FluidStack fluid1, FluidStack fluid2){
        if(fluid1 == null && fluid2 == null){
            return true;
        }
        if(fluid1 != null && fluid2 == null || fluid1 == null){
            return false;
        }
        return fluid1.isFluidStackIdentical(fluid2);
    }
}
