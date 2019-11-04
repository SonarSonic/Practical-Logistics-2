package sonar.logistics.base.data.inventory;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.newinfo.INewInfo;
import sonar.logistics.base.data.readers.AbstractDataReader;

//TODO
public class InventoryDataReader extends AbstractDataReader {

    public InventoryDataReader(InfoUUID uuid) {
        super(uuid);
    }

    @Override
    public INewInfo update(INewInfo current) {
        return null;
    }

    public void onDataChanged(DataHolder holder){

    }
}
