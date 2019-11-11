package sonar.logistics.base.data.types.inventory;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.newinfo.INewInfo;
import sonar.logistics.base.data.newinfo.AbstractDataReader;
import sonar.logistics.base.data.sources.MultiDataSource;

public class InventoryDataReader extends AbstractDataReader {

    public MultiDataSource sources;
    public DataHolder<InventoryData> inventoryData;

    public InventoryDataReader(InfoUUID uuid, MultiDataSource sources) {
        super(uuid);
        this.sources = sources;
        //this.inventoryData = DataManager.instance().getOrCreateDataHolder(InventoryData.class, sources, 20); FIXME!
    }

    @Override
    public INewInfo update(INewInfo current) {
        if(!(current instanceof InventoryInfo)){
            current = new InventoryInfo(uuid);
            ((InventoryInfo) current).inventoryData = inventoryData.data;
            return current;
        }
        return current;
    }

    public void onDataChanged(DataHolder holder){
        System.out.println("data!");
    }
}
