package sonar.logistics.base.data.holders;

import sonar.core.SonarCore;
import sonar.logistics.base.data.api.IData;
import sonar.logistics.base.data.api.IDataGenerator;
import sonar.logistics.base.data.api.IDataWatcher;
import sonar.logistics.base.data.sources.IDataSource;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    public final IDataGenerator generator;
    public final List<IDataWatcher> dataWatchers;
    public final IDataSource source;
    public IData data;
    public int ticks;
    public int tickRate;
    public boolean hasWatchers = false;

    public DataHolder(IDataGenerator generator, IDataSource source, IData freshData, int tickRate){
        this.generator = generator;
        this.dataWatchers = new ArrayList<>();
        this.source = source;
        this.data = freshData;
        this.tickRate = tickRate;
        this.ticks = SonarCore.randInt(0, tickRate); ///attempting to distribute updates more evenly
    }

    public boolean canUpdateData() {
        return !dataWatchers.isEmpty() && ticks == tickRate;
    }

    public void onWatchersChanged() {
        if(dataWatchers.isEmpty()){
            ///FIXME INVALIDATE DATA HOLDER, THIS WOULD MEAN NOTHING CARES ABOUT THE DATA ANYMORE?
            hasWatchers = false;
            return;
        }

        for (IDataWatcher watcher : dataWatchers) {
            if (watcher.isWatcherActive()) {
                hasWatchers = true;
                return;
            }
        }
        hasWatchers = false;
    }

    public void tick() {
        if(!hasWatchers || !dataWatchers.isEmpty()){
            return;
        }
        if(ticks>=tickRate) {
            ticks = 0;
        }else{
            ticks++;
        }
    }


    public void addWatcher(IDataWatcher watcher){
        dataWatchers.add(watcher);
    }

    public void removeWatcher(IDataWatcher watcher){
        dataWatchers.remove(watcher);
    }

    public void onDataChanged(){
        dataWatchers.forEach(w -> w.onDataChanged(this));
    }

    public void onHolderDestroyed(){}
}
