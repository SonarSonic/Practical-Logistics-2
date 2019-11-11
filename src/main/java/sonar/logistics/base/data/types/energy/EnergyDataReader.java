package sonar.logistics.base.data.types.energy;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.newinfo.INewInfo;
import sonar.logistics.base.data.newinfo.AbstractDataReader;
import sonar.logistics.base.data.sources.MultiDataSource;

public class EnergyDataReader extends AbstractDataReader {

    public MultiDataSource sources;
    public DataHolder<EnergyStorageData> energyData;

    public EnergyDataReader(InfoUUID uuid, MultiDataSource sources) {
        super(uuid);
        this.sources = sources;
        ///this.energyData = DataManager.instance().getOrCreateDataHolder(EnergyData.class, sources, 20); //FIXME
    }

    @Override
    public INewInfo update(INewInfo current) {
        if(!(current instanceof EnergyInfo)){
            current = new EnergyInfo(uuid);
            ((EnergyInfo) current).energyData = energyData.data;
            return current;
        }
        return current;
    }

    public void onDataChanged(DataHolder holder){
        System.out.println("data!");
    }
}
