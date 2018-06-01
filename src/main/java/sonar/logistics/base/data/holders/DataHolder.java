package sonar.logistics.base.data.holders;

import sonar.core.SonarCore;
import sonar.logistics.base.data.api.IData;
import sonar.logistics.base.data.api.IDataGenerator;
import sonar.logistics.base.data.api.IDataHolder;
import sonar.logistics.base.data.api.IDataWatcher;
import sonar.logistics.base.data.sources.IDataSource;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DataHolder<T extends IData, H extends IDataHolder<T>> implements IDataHolder<T> {

    public final IDataGenerator<IDataSource, T, H> generator;
    public final List<IDataWatcher> dataWatchers;
    public final IDataSource source;
    public T data;
    public int ticks;
    public int tickRate;
    public boolean hasWatchers = false;

    public DataHolder(IDataGenerator<IDataSource, T, H> generator, IDataSource source, T freshData, int tickRate){
        this.generator = generator;
        this.dataWatchers = new ArrayList<>();
        this.source = source;
        this.data = freshData;
        this.tickRate = tickRate;
        this.ticks = SonarCore.randInt(0, tickRate); ///attempting to distribute updates more evenly
    }

    @Override
    public IDataGenerator getGenerator() {
        return generator;
    }

    @Override
    public List<IDataWatcher> getDataWatchers() {
        return dataWatchers;
    }

    @Override
    public boolean canUpdateData() {
        return !dataWatchers.isEmpty() && ticks == tickRate;
    }

    @Override
    public T getData() {
        return data;
    }

    @Nonnull
    @Override
    public IDataSource getSource() {
        return source;
    }

    @Override
    public void setData(T value) {
        data = value;
    }

    @Override
    public boolean hasWatchers() {
        return hasWatchers;
    }

    @Override
    public void setWatchers(boolean setWatchers) {
        this.hasWatchers = setWatchers;
    }

    @Override
    public void onWatchersChanged() {
        if(getDataWatchers().isEmpty()){
            ///FIXME INVALIDATE DATA HOLDER.
            setWatchers(false);
            return;
        }

        for (IDataWatcher watcher : getDataWatchers()) {
            if (watcher.isWatched()) {
                setWatchers(true);
                return;
            }
        }
        setWatchers(false);
    }

    @Override
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

    @Override
    public void setTick(int tick) {
        ticks = tick;
    }
}
