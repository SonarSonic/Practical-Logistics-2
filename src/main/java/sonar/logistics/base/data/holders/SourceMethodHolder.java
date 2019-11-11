package sonar.logistics.base.data.holders;

import sonar.logistics.base.data.api.IEnvironment;
import sonar.logistics.base.data.api.methods.IMethod;
import sonar.logistics.base.data.sources.IDataSource;

import java.util.HashMap;
import java.util.Map;

public class SourceMethodHolder {

    public IDataSource source;
    public IEnvironment environment;
    public Map<IMethod, DataHolder> holders = new HashMap<>();

    public SourceMethodHolder(IDataSource source, IEnvironment environment){
        this.source = source;
        this.environment = environment;
    }

    public void updateData(){
        for(Map.Entry<IMethod, DataHolder> entry : holders.entrySet()){
            entry.getValue().tick(); //FIXME change to the tick bit, this UPDATES EVERY TICK AT THE MO, BAD PERFORMANCE ATM!
            if(/*holder.canUpdateData() && */entry.getKey().canInvoke(environment)){ //FIXME

                boolean isNew = false;
                if(entry.getValue().data == null){
                    entry.getValue().data = entry.getKey().getDataFactory().create();
                    isNew = true;
                }

                entry.getValue().data.preUpdate();
                Object obj =  entry.getKey().invoke(environment);
                entry.getKey().getDataFactory().updateData(entry.getValue().data, obj);
                entry.getValue().data.postUpdate();

                if(isNew){
                    entry.getValue().onDataChanged();
                    entry.getValue().data.onUpdated();
                    ///SEND NBT!
                }else if(entry.getValue().data.hasUpdated()){
                    System.out.println(entry.getKey().getIdentifier() + " "  + obj);
                    entry.getValue().onDataChanged();
                    entry.getValue().data.onUpdated();
                    ///SEND UPDATE!!!!!
                }
            }
        }
    }

}
