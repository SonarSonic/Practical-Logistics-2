package sonar.logistics.core.tiles.displays.info.newinfoprofiding.api;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.listeners.ILogicListenable;

import javax.annotation.Nullable;

/**resposible for converting data into viewable info, also sends info to client*/
public interface IDataConvertor<L extends ILogicListenable> extends IDataWatcher {

    /**returns the updated info
     * @param current*/
    IInfo update(@Nullable IInfo current);

    InfoUUID getUUID();

    L getSourceTile();

    default boolean forceFullPacket(){
        return false;
    }
}
