package sonar.logistics.core.tiles.displays.info.newinfoprofiding.api;

import java.util.List;

/**refers to any object which watches the changes of a piece of IData*/
public interface IDataWatcher {

    boolean isWatched();

    List<IDataHolder> getDataHolders();

    default void onDataChanged(IDataHolder holder){}
}
