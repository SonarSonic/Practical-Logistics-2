package sonar.logistics.core.tiles.displays.info.newinfoprofiding.api;

import sonar.logistics.core.tiles.displays.info.newinfoprofiding.sources.IDataSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**a container used to reference a particular {@IData},
 * it handles how often the data is updated */
public interface IDataHolder<T extends IData> {

    default boolean isValid(){return true;};

    List<IDataWatcher> getDataWatchers();

    default void addWatcher(IDataWatcher watcher){
        getDataWatchers().add(watcher);
    }

    default void removeWatcher(IDataWatcher watcher){
        getDataWatchers().remove(watcher);
    }

    default void onWatchersChanged(){}

    default void onDataChanged(){
        getDataWatchers().forEach(w -> w.onDataChanged(this));
    }

    default void onHolderDestroyed(){
        
    }

    @Nullable
    IDataGenerator<IDataSource, T, IDataHolder<T>> getGenerator();

    boolean canUpdateData();

    @Nonnull
    T getData();

    @Nonnull
    IDataSource getSource();

    void setData(T value);

    boolean hasWatchers();

    void setWatchers(boolean hasWatchers);

    void tick();

    void setTick(int tick);
}
