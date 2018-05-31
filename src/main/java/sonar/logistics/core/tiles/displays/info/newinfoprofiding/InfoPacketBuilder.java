package sonar.logistics.core.tiles.displays.info.newinfoprofiding;

import net.minecraft.entity.player.EntityPlayerMP;
import sonar.core.helpers.NBTHelper;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.core.tiles.displays.info.newinfoprofiding.api.IData;

import java.util.List;
import java.util.Map;


///TODO
public class InfoPacketBuilder {

    public Map<EntityPlayerMP, Map<IInfo, NBTHelper.SyncType>> changed_info;
    public Map<EntityPlayerMP, Map<IData, NBTHelper.SyncType>> changed_data;

}
