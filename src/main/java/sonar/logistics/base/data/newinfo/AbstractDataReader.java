package sonar.logistics.base.data.newinfo;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.data.api.IDataReader;
import sonar.logistics.base.data.holders.DataHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataReader implements IDataReader {

    public final InfoUUID uuid;
    public List<DataHolder> holders = new ArrayList();

    public AbstractDataReader(InfoUUID uuid){
        this.uuid = uuid;
    }

    @Override
    public InfoUUID getUUID() {
        return uuid;
    }

    @Override
    public boolean isWatcherActive() {
        return true; //TODO FIXME!
    }

    @Override
    public List<DataHolder> getDataHolders() {
        return holders;
    }
}
