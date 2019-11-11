package sonar.logistics.core.tiles.readers.items;

import sonar.logistics.base.data.DataManager;
import sonar.logistics.base.data.generators.InventoryDataGenerator;
import sonar.logistics.base.data.sources.MultiDataSource;
import sonar.logistics.base.data.sources.SourceCoord4D;
import sonar.logistics.base.data.types.inventory.InventoryDataReader;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.core.tiles.readers.base.TileAbstractNewReader;

import java.util.List;

public class TileInventoryReader extends TileAbstractNewReader {

    public InventoryDataReader reader;

    public void onTileAddition(){
        super.onTileAddition();
        List<SourceCoord4D> sources = MultiDataSource.getDirtyConversion(network.getConnections(CacheType.ALL));
        sources.forEach(source -> DataManager.instance().addDataSource(source));
        DataManager.instance().addDataGenerator(new InventoryDataGenerator(), 20, sources);
        //DataManager.instance().addWatcher(reader = new InventoryDataReader(new InfoUUID(identity, 0), new MultiDataSource(MultiDataSource.getDirtyConversion(network.getConnections(CacheType.ALL)))));
    }


    public void onTileRemoval() {
        super.onTileRemoval();
        DataManager.instance().removeAll();
        //DataManager.instance().removeWatcher(reader);
    }
}
