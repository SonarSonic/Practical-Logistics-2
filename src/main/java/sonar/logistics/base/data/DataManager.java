package sonar.logistics.base.data;

import com.google.common.collect.Lists;
import sonar.core.helpers.FunctionHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.data.api.*;
import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.holders.DataHolderMultiSource;
import sonar.logistics.base.data.inventory.InventoryData;
import sonar.logistics.base.data.inventory.InventoryDataFactory;
import sonar.logistics.base.data.inventory.InventoryDataGenerator;
import sonar.logistics.base.data.sources.IDataMultiSource;
import sonar.logistics.base.data.sources.IDataSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class DataManager {


    private Map<Class, List<IDataGenerator>> GENERATORS = new HashMap<>();
    private Map<Class, IDataFactory> FACTORIES = new HashMap<>();


    private List<IDataWatcher> addedWatchers = new ArrayList<>();
    private List<IDataWatcher> removedWatchers = new ArrayList<>();
    private Map<InfoUUID, IDataWatcher> LOADED_WATCHERS = new HashMap<>();
    private Map<IDataSource, List<DataHolder>> HOLDER_SOURCE_MAP = new HashMap<>();
    private Map<IDataMultiSource, List<DataHolderMultiSource>> HOLDER_MULTI_SOURCE_MAP = new HashMap<>();

    {
        GENERATORS.computeIfAbsent(InventoryData.class, (c) -> new ArrayList<>()).add(new InventoryDataGenerator());
        FACTORIES.put(InventoryData.class, new InventoryDataFactory());

    }

    public static DataManager instance(){
        return PL2.proxy.dataFactory;
    }

    public void removeAll(){
        addedWatchers.clear();
        removedWatchers.clear();
        LOADED_WATCHERS.clear();
        HOLDER_SOURCE_MAP.clear();
        HOLDER_MULTI_SOURCE_MAP.clear();
    }

    public void flushWatchers(){
        addedWatchers.forEach(watcher -> watcher.getDataHolders().stream().filter(Objects::nonNull).forEach(h -> h.addWatcher(watcher)));
        removedWatchers.forEach(watcher -> watcher.getDataHolders().stream().filter(Objects::nonNull).forEach(h -> h.removeWatcher(watcher)));

        addedWatchers.clear();
        removedWatchers.clear();
    }

    public void flushUpdates(){
        HOLDER_SOURCE_MAP.values().forEach(l -> l.forEach(h -> {
            h.tick();
            if(h.canUpdateData()){
                h.generator.updateData(h, h.data, h.source);
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
    public static <S extends IDataSource, D extends IData> IDataGenerator<S, D> getValidGenerator(S source, Class<D> dataType){
        return instance().GENERATORS.get(dataType).stream().filter(d -> d.canGenerateForSource(source)).findFirst().get();
    }


    @Nonnull
    public static <D extends IData> IDataFactory<D> getFactory(Class<D> dataType){
        IDataFactory factory = instance().FACTORIES.get(dataType);
        if(factory == null){
            throw new NullPointerException("NO DATA FACTORY FOR: " + dataType);
        }
        return factory;
    }

    @Nonnull
    public <D extends IData> DataHolder getOrCreateDataHolder(Class<D> dataType, IDataSource source, int tickRate){
        DataHolder holder = getDataHolder(dataType, source);
        if(holder != null){
            return holder;
        }

        if(source instanceof IDataMultiSource) {
            IDataMultiSource multiSource = (IDataMultiSource) source;
            DataHolderMultiSource newMultiHolder = new DataHolderMultiSource(multiSource, getFactory(dataType), tickRate);
            multiSource.getDataSources().forEach(s -> {
                DataHolder dataHolder = getOrCreateDataHolder(dataType, s, tickRate);
                newMultiHolder.addDataHolder(dataHolder);
                dataHolder.addWatcher(newMultiHolder);
            });
            HOLDER_MULTI_SOURCE_MAP.computeIfAbsent(multiSource, FunctionHelper.ARRAY).add(newMultiHolder);
            return newMultiHolder;
        }else{
            DataHolder newHolder = new DataHolder(getValidGenerator(source, dataType), source, getFactory(dataType).create(), tickRate);
            HOLDER_SOURCE_MAP.computeIfAbsent(source, FunctionHelper.ARRAY).add(newHolder);
            return newHolder;
        }
    }


    @Nullable
    public <D extends IData> DataHolder getDataHolder(Class<D> dataType, IDataSource source){
        List<? extends DataHolder> holders = (source instanceof IDataMultiSource? HOLDER_MULTI_SOURCE_MAP : HOLDER_SOURCE_MAP).get(source);
        if(holders != null && !holders.isEmpty()){
            for(DataHolder holder : holders){
                if(dataType == holder.data.getClass()){
                    return holder;
                }
            }
        }
        return null;
    }

    /*
    public void removeDataSource(IDataSource source){
        List<IDataHolder> holders = HOLDER_SOURCE_MAP.get(source);
        if(holders != null && !holders.isEmpty()){
            holders.forEach(h -> h.onHolderDestroyed());
            holders.clear();
        }
    }
    */

    public Map<InfoUUID, IDataWatcher> getDataWatchers(){
        return LOADED_WATCHERS;
    }

    public void addWatcher(IDataWatcher watcher){
        addedWatchers.add(watcher);
    }

    public void removeWatcher(IDataWatcher watcher){
        removedWatchers.add(watcher);
    }

    public void onWatcherChanged(IDataWatcher watcher){
        watcher.getDataHolders().forEach(holder -> holder.onWatchersChanged());
    }

    public void onMultiSourceChanged(IDataMultiSource multiSource){
        List<DataHolderMultiSource> holders = HOLDER_MULTI_SOURCE_MAP.get(multiSource);
        if(holders !=null && !holders.isEmpty()) {
            //holders.forEach(mHolder -> mHolder.getDataHolders().forEach(holder -> ((IDataHolder)holder).removeWatcher(mHolder)));
            for(DataHolderMultiSource mHolder : holders) {
                List<DataHolder> oldHolders = Lists.newArrayList(mHolder.subDataHolders);
                List<DataHolder> newHolders = new ArrayList<>();
                multiSource.getDataSources().forEach(s -> {
                    newHolders.add(getOrCreateDataHolder(mHolder.data.getClass(), s, mHolder.tickRate));
                });
                List<DataHolder> removed = new ArrayList<>();
                for (DataHolder ref : oldHolders) {
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
            if(entry.getValue().isWatcherActive()){
                IInfo oldInfo = ServerInfoHandler.instance().getInfoMap().get(entry.getKey());
               // IInfo newInfo = entry.createValue().update(oldInfo);
            }
        }
    }

}
