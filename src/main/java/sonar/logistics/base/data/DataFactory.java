package sonar.logistics.base.data;

import com.google.common.collect.Lists;
import sonar.core.helpers.FunctionHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.data.api.*;
import sonar.logistics.base.data.holders.DataHolderEmpty;
import sonar.logistics.base.data.holders.DataHolderMultiSource;
import sonar.logistics.base.data.sources.IDataMultiSource;
import sonar.logistics.base.data.sources.IDataSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class DataFactory {

    public List<IDataGenerator> GENERATORS = new ArrayList<>();
    public List<IDataWatcher> ADDED_WATCHERS = new ArrayList<>();
    public List<IDataWatcher> REMOVED_WATCHERS = new ArrayList<>();
    public Map<InfoUUID, IDataWatcher> LOADED_WATCHERS = new HashMap<>();
    public Map<IDataSource, List<IDataHolder>> HOLDER_SOURCE_MAP = new HashMap<>();
    public Map<IDataMultiSource, List<DataHolderMultiSource>> HOLDER_MULTI_SOURCE_MAP = new HashMap<>();

    public static DataFactory instance(){
        return PL2.proxy.dataFactory;
    }

    public void removeAll(){
        ADDED_WATCHERS.clear();
        REMOVED_WATCHERS.clear();
        LOADED_WATCHERS.clear();
        HOLDER_SOURCE_MAP.clear();
        HOLDER_MULTI_SOURCE_MAP.clear();
    }

    public void flushWatchers(){
        ADDED_WATCHERS.forEach(watcher -> watcher.getDataHolders().stream().filter(h -> h != null).forEach(h -> h.addWatcher(watcher)));
        REMOVED_WATCHERS.forEach(watcher -> watcher.getDataHolders().stream().filter(h -> h != null).forEach(h -> h.removeWatcher(watcher)));

        ADDED_WATCHERS.clear();
        REMOVED_WATCHERS.clear();
    }

    public void flushUpdates(){
        HOLDER_SOURCE_MAP.values().forEach(l -> l.forEach(h -> {
            h.tick();
            if(h.canUpdateData()){
                h.getGenerator().generateData(h, h.getData(), h.getSource());
                h.onDataChanged();
            }
        }));
        HOLDER_MULTI_SOURCE_MAP.values().forEach(l -> l.forEach(h -> {
            h.tick();
            if(h.canUpdateData()){
                h.updateMultiSourceData();
            }
        }));
    }

    @Nonnull
    public <D extends IData> IDataHolder<D> getOrCreateDataHolder(IDataSource source, IDataFactory<D> factory, int tickRate){
        Optional<IDataHolder<D>> holder = getDataHolder(source, factory.create());
        if(holder.isPresent()) {
            return holder.get();
        }
        if(source instanceof IDataMultiSource) {
            IDataMultiSource multiSource = (IDataMultiSource) source;
            DataHolderMultiSource newHolder = new DataHolderMultiSource(multiSource, factory, tickRate);
            multiSource.getDataSources().forEach(s -> {
                IDataHolder created = getOrCreateDataHolder(s, factory, tickRate);
                if(created.isValid()){
                    newHolder.addDataHolder(created);
                }
            });
            if(!newHolder.getDataHolders().isEmpty()) {
                newHolder.getDataHolders().forEach(h -> ((IDataHolder) h).addWatcher(newHolder));
                HOLDER_MULTI_SOURCE_MAP.computeIfAbsent(multiSource, FunctionHelper.ARRAY).add(newHolder);
                return newHolder;
            }
        }else{
            D data = factory.create();
            IDataHolder<D> newHolder = data.createHolder(source, data, tickRate);
            if(newHolder != null) {
                HOLDER_SOURCE_MAP.computeIfAbsent(source, FunctionHelper.ARRAY).add(newHolder);
                return newHolder;
            }
        }
        return new DataHolderEmpty(source, factory.create(), tickRate);
    }

    public <D extends IData> Optional<IDataHolder<D>> getDataHolder(IDataSource source, D falseData){
        List<? extends IDataHolder> holders = (source instanceof IDataMultiSource? HOLDER_MULTI_SOURCE_MAP : HOLDER_SOURCE_MAP).get(source);
        if(holders != null && !holders.isEmpty()){
            for(IDataHolder holder : holders){
                if(falseData.getClass() == holder.getData().getClass()){
                    return Optional.of(holder);
                }
            }
        }
        return Optional.empty();
    }


    @Nullable
    public Optional<IDataGenerator> getValidGenerator(IDataSource source, IData data){
        return GENERATORS.stream().filter(G -> G.canGenerateForSource(source) && G.canGenerateForData(data)).findFirst();
    }

    public void addDataSource(IDataSource source){

    }

    public void removeDataSource(IDataSource source){
        List<IDataHolder> holders = HOLDER_SOURCE_MAP.get(source);
        if(holders != null && !holders.isEmpty()){
            holders.forEach(h -> h.onHolderDestroyed());
            holders.clear();
        }
    }

    public List<IDataGenerator> getDataGenerators(){
        return GENERATORS;
    }

    public Map<InfoUUID, IDataWatcher> getDataWatchers(){
        return LOADED_WATCHERS;
    }

    public void addWatcher(IDataWatcher watcher){
        ADDED_WATCHERS.add(watcher);
    }

    public void removeWatcher(IDataWatcher watcher){
        REMOVED_WATCHERS.add(watcher);
    }

    public void onWatcherChanged(IDataWatcher watcher){
        watcher.getDataHolders().forEach(holder -> holder.onWatchersChanged());
    }

    public void onMultiSourceChanged(IDataMultiSource multiSource){
        List<DataHolderMultiSource> holders = HOLDER_MULTI_SOURCE_MAP.get(multiSource);
        if(holders !=null && !holders.isEmpty()) {
            //holders.forEach(mHolder -> mHolder.getDataHolders().forEach(holder -> ((IDataHolder)holder).removeWatcher(mHolder)));
            for(DataHolderMultiSource mHolder : holders) {
                List<IDataHolder> oldHolders = Lists.newArrayList(mHolder.subDataHolders);
                List<IDataHolder> newHolders = new ArrayList<>();
                multiSource.getDataSources().forEach(s -> {
                    newHolders.add(getOrCreateDataHolder(s, mHolder.factory, mHolder.tickRate));
                });
                List<IDataHolder> removed = new ArrayList<>();
                for (IDataHolder ref : oldHolders) {
                    if (!newHolders.contains(ref)) {
                        removed.add(ref);
                        continue;
                    }
                    newHolders.remove(ref);
                }

                if (!newHolders.isEmpty() || !removed.isEmpty()) {
                    newHolders.forEach(holder -> mHolder.addDataHolder(holder));
                    removed.forEach(holder -> mHolder.removeDataHolder(holder));
                }
            }
        }

    }

    public void sendInfoPackets(){
        for(Map.Entry<InfoUUID, IDataWatcher> entry : LOADED_WATCHERS.entrySet()){
            if(entry.getValue().isWatched()){
                IInfo oldInfo = ServerInfoHandler.instance().getInfoMap().get(entry.getKey());
               // IInfo newInfo = entry.createValue().update(oldInfo);
            }
        }
    }

}
