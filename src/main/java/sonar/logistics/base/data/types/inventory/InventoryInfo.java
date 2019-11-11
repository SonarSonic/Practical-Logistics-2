package sonar.logistics.base.data.types.inventory;

import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.data.DataManager;
import sonar.logistics.base.data.newinfo.BaseNewInfo;

public class InventoryInfo extends BaseNewInfo {

    public InventoryData inventoryData = DataManager.getFactoryForData(InventoryData.class).create();

    public InventoryInfo(InfoUUID uuid) {
        super(uuid);
    }

    public void render(){
        //TODO ADD DATA INVENTORY ELEMENTS!
    }

    @Override
    public void save(NBTTagCompound tag) {
        DataManager.getFactory(InventoryData.class).save(inventoryData, "inventory", tag);
    }

    @Override
    public void read(NBTTagCompound tag) {
        DataManager.getFactory(InventoryData.class).read(inventoryData, "inventory", tag);
    }

}