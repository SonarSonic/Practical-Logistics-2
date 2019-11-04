package sonar.logistics.base.data.newinfo;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;

public abstract class BaseNewInfo implements INewInfo {

    public final InfoUUID uuid;

    public BaseNewInfo(InfoUUID uuid){
        this.uuid = uuid;
    }

    @Override
    public InfoUUID getUUID() {
        return uuid;
    }

}
