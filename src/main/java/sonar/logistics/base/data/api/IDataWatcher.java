package sonar.logistics.base.data.api;

import sonar.logistics.base.data.holders.DataHolder;

import java.util.List;

/**refers to any object which watches the changes of a piece of IData*/
public interface IDataWatcher {

    /**if the data watcher is currently activated by a viewer (generally a player)*/
    boolean isWatcherActive();

    /**the list of Data Holders the watcher is currently waiting for data from.*/
    List<DataHolder> getDataHolders();

    default void onDataChanged(DataHolder holder){}
}
