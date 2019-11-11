package sonar.logistics.base.data.listeners;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

/**simplification of the over complicated PL2ListenerList*/
public class DataListeners implements IDataListeners {

    public List<EntityPlayerMP> listeners;

    @Override
    public List<EntityPlayerMP> getListeners() {
        return listeners;
    }
}
