package sonar.logistics.core.tiles.displays.info.newinfoprofiding;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.core.tiles.displays.info.newinfoprofiding.api.IDataConvertor;

public abstract class AbstractDataConvertor<L extends ILogicListenable> implements IDataConvertor<L> {

    public InfoUUID uuid;
    public L source;

    public AbstractDataConvertor(InfoUUID uuid, L source){
        this.uuid = uuid;
        this.source = source;
    }

    @Override
    public InfoUUID getUUID() {
        return uuid;
    }

    @Override
    public L getSourceTile() {
        return source;
    }

    public boolean isWatched() {
        ///FIXME
        return getSourceTile().getListenerList().hasListeners();
    }
}
