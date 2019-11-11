package sonar.logistics.base.data.types.energy;

import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.data.DataManager;
import sonar.logistics.base.data.newinfo.BaseNewInfo;

public class EnergyInfo extends BaseNewInfo {

    public EnergyStorageData energyData = DataManager.getFactoryForData(EnergyStorageData.class).create();

    public EnergyInfo(InfoUUID uuid) {
        super(uuid);
    }

    public void render(){
        //TODO ADD DATA INVENTORY ELEMENTS!
    }

    @Override
    public void save(NBTTagCompound tag) {
        DataManager.getFactoryForData(EnergyStorageData.class).save(energyData, "energy", tag);
    }

    @Override
    public void read(NBTTagCompound tag) {
        DataManager.getFactoryForData(EnergyStorageData.class).read(energyData, "energy", tag);
    }

}