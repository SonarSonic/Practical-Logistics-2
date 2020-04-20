package sonar.logistics.base.data.api;

import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.base.data.inventory.InventoryData;

public interface IDataFactory<D extends IData>{

        D create();

        void save(InventoryData data, NBTTagCompound tag);

        void read(InventoryData data, NBTTagCompound tag);

}