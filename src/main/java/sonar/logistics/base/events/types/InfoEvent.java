package sonar.logistics.base.events.types;

import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;

public class InfoEvent extends Event {

    public final InfoUUID uuid;
    public final boolean isRemote;

    public InfoEvent(InfoUUID uuid, boolean isRemote) {
        super();
        this.uuid = uuid;
        this.isRemote = isRemote;
    }

    public static class InfoChanged extends InfoEvent{

        public final IInfo newInfo;

        public InfoChanged(IInfo newInfo, InfoUUID uuid, boolean isRemote){
            super(uuid, isRemote);
            this.newInfo = newInfo;
        }

    }

    public static class ListChanged extends InfoEvent{

        public final AbstractChangeableList newList;

        public ListChanged(AbstractChangeableList newList, InfoUUID uuid, boolean isRemote){
            super(uuid, isRemote);
            this.newList = newList;
        }
    }
}
