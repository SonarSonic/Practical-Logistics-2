package sonar.logistics.base.data.newinfo;

import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;

public interface INewInfo {

    InfoUUID getUUID();

    void save(NBTTagCompound tag);

    void read(NBTTagCompound tag);
}
