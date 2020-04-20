package sonar.logistics.base.data.holders;

import sonar.logistics.base.data.api.IDataCombinable;
import sonar.logistics.base.data.api.IDataFactory;
import sonar.logistics.base.data.api.IDataWatcher;
import sonar.logistics.base.data.sources.IDataMultiSource;

import java.util.ArrayList;
import java.util.List;

public class DataHolderMultiSource<D extends IDataCombinable> extends DataHolder implements IDataWatcher {

    public final List<DataHolder> subDataHolders;
    public final List<DataHolder> changedHolders;
    public final IDataFactory<D> factory;

    public DataHolderMultiSource(IDataMultiSource source, IDataFactory<D> factory, int tickRate) {
        super(null, source, factory.create(), tickRate);
        subDataHolders = new ArrayList<>();
        changedHolders = new ArrayList<>();
        this.factory = factory;
    }

    public void addDataHolder(DataHolder holder){
        if(holder != null)
            subDataHolders.add(holder);
    }

    public void removeDataHolder(DataHolder holder){
        if(holder != null)
            subDataHolders.remove(holder);
    }

    @Override
    public boolean isWatcherActive() {
        return hasWatchers;
    }

    @Override
    public List<DataHolder> getDataHolders() {
        return subDataHolders;
    }

    @Override
    public void onDataChanged(DataHolder holder){
        if(holder.data instanceof IDataCombinable){
            changedHolders.add(holder);
        }
    }

    @Override
    public void onWatchersChanged(){
        super.onWatchersChanged();
        subDataHolders.forEach(h -> h.onWatchersChanged());
    }

    @Override
    public boolean canUpdateData() {
        return !changedHolders.isEmpty() && super.canUpdateData();
    }

    public void updateMultiSourceData(){
        boolean changed = false;
        for(DataHolder holder : changedHolders) {
            if (((IDataCombinable)data).canCombine((IDataCombinable)holder.data)) {
                if(((IDataCombinable)data).doCombine((IDataCombinable)holder.data)){
                    changed = true;
                }
            }
        }
        changedHolders.clear();

        if(changed){
            onDataChanged();
        }

    }

}
