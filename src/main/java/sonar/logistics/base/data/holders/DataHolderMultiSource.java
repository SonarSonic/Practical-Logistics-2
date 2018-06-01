package sonar.logistics.base.data.holders;

import sonar.logistics.base.data.api.IDataCombinable;
import sonar.logistics.base.data.api.IDataFactory;
import sonar.logistics.base.data.api.IDataHolder;
import sonar.logistics.base.data.api.IDataWatcher;
import sonar.logistics.base.data.sources.IDataMultiSource;

import java.util.ArrayList;
import java.util.List;

public class DataHolderMultiSource<D extends IDataCombinable> extends DataHolder<D, DataHolderMultiSource<D>> implements IDataWatcher {

    public final List<IDataHolder> subDataHolders;
    public final List<IDataHolder<IDataCombinable>> changedHolders;
    public final IDataFactory<D> factory;

    public DataHolderMultiSource(IDataMultiSource source, IDataFactory<D> factory, int tickRate) {
        super(null, source, factory.create(), tickRate);
        subDataHolders = new ArrayList<>();
        changedHolders = new ArrayList<>();
        this.factory = factory;

    }

    public void addDataHolder(IDataHolder<D> holder){
        if(holder != null)
            subDataHolders.add(holder);
    }

    public void removeDataHolder(IDataHolder<D> holder){
        if(holder != null)
            subDataHolders.remove(holder);
    }

    @Override
    public boolean isWatched() {
        return this.hasWatchers();
    }

    @Override
    public List<IDataHolder> getDataHolders() {
        return subDataHolders;
    }

    @Override
    public void onDataChanged(IDataHolder holder){
        if(holder.getData() instanceof IDataCombinable){
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
        for(IDataHolder<IDataCombinable> holder : changedHolders) {
            if (getData().canCombine(holder.getData())) {
                if(getData().doCombine(holder.getData())){
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
