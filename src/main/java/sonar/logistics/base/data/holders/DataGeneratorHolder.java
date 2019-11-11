package sonar.logistics.base.data.holders;

import sonar.logistics.base.data.DataManager;
import sonar.logistics.base.data.api.IData;
import sonar.logistics.base.data.api.IDataFactory;
import sonar.logistics.base.data.api.IDataGenerator;
import sonar.logistics.base.data.api.IDataWatcher;

import java.util.ArrayList;
import java.util.List;

public class DataGeneratorHolder<D extends IData> extends DataHolder<D> implements IDataWatcher {

    final IDataGenerator<D> generator;
    final IDataFactory<D> factory;
    //FIXME TO ADD MULTI-SOURCE which sends updates etc..

    List<DataHolder> sub_holders = new ArrayList<>();

    public boolean updateAllData = true;

    public DataGeneratorHolder(IDataGenerator<D> generator, int tickRate){
        super(tickRate);
        this.generator = generator;
        this.factory = DataManager.getFactoryForData(generator.getDataType());
    }

    @Override
    public boolean isWatcherActive() {
        return true;///dataHolder.hasWatchers; // FIXME
    }

    @Override
    public List<DataHolder> getDataHolders() {
        return sub_holders;
    }

    public void doTick(){
        if(updateAllData) {

            boolean isNew = false;
            if(data == null){
                data = factory.create();
                isNew = true;
            }

            generator.generateData(data,sub_holders);
            ///FIXME MAKE UPDATEDATA VERSION WHEN ONLY CERTAIN SOURCES CHANGE ETC.
            updateAllData=false;

            if(isNew){
                data.onUpdated();
            }
            else if(data.hasUpdated()){
                System.out.println(data);
                data.onUpdated();
            }
        }
    }

    @Override
    public void onDataChanged(DataHolder holder){
        updateAllData = true;
    }

    @Override
    public void addDataHolder(DataHolder holder){
        sub_holders.add(holder);
        updateAllData = true;
    }

    @Override
    public void removeDataHolder(DataHolder holder){
        sub_holders.remove(holder);
        updateAllData = true;

    }

}
