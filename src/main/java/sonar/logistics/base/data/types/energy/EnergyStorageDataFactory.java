package sonar.logistics.base.data.types.energy;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;
import sonar.core.api.energy.EnergyType;
import sonar.logistics.base.data.api.IDataFactory;

public class EnergyStorageDataFactory implements IDataFactory<EnergyStorageData> {

    public static final String ENERGY_TYPE = "type";
    public static final String ENERGY = "energy";
    public static final String CAPACITY = "capacity";

    @Override
    public EnergyStorageData create() {
        return new EnergyStorageData();
    }

    @Override
    public void save(EnergyStorageData data, String key, NBTTagCompound tag) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(ENERGY_TYPE, data.type.ordinal());
        nbt.setLong(ENERGY, data.energy);
        nbt.setLong(CAPACITY, data.energy);
        tag.setTag(key, nbt);
    }

    @Override
    public void read(EnergyStorageData data, String key, NBTTagCompound tag) {
        NBTTagCompound nbt = tag.getCompoundTag(key);
        data.type = EnergyType.values()[nbt.getInteger(ENERGY_TYPE)];
        data.energy = nbt.getLong(ENERGY);
        data.capacity = nbt.getLong(CAPACITY);
    }

    @Override
    public void saveUpdate(EnergyStorageData data, ByteBuf buf) {
        buf.writeLong(data.energy);
        buf.writeLong(data.capacity);
    }

    @Override
    public void readUpdate(EnergyStorageData data, ByteBuf buf) {
        data.energy = buf.readLong();
        data.capacity = buf.readLong();
    }

    @Override
    public boolean canConvert(Class returnType){
        return returnType == IEnergyStorage.class;
    }

    @Override
    public void updateData(EnergyStorageData data, Object obj){
        if(obj instanceof IEnergyStorage) {
            IEnergyStorage storage = (IEnergyStorage)obj;
            data.type = EnergyType.FE;

            if (data.energy != storage.getEnergyStored()) {
                data.energy = storage.getEnergyStored();
                data.hasUpdated = true;
            }

            if (data.capacity != storage.getMaxEnergyStored()) {
                data.capacity = storage.getMaxEnergyStored();
                data.hasUpdated = true;
            }
        }
    }
}
