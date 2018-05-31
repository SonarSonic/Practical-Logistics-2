package sonar.logistics.core.tiles.displays.info.newinfoprofiding.generators;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.ISonarInventoryHandler;
import sonar.core.api.inventories.StoredItemStack;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.core.tiles.displays.info.newinfoprofiding.api.IData;
import sonar.logistics.core.tiles.displays.info.newinfoprofiding.api.IDataGenerator;
import sonar.logistics.core.tiles.displays.info.newinfoprofiding.sources.IDataSource;
import sonar.logistics.core.tiles.displays.info.newinfoprofiding.holders.DataHolder;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DataGeneratorItemList implements IDataGenerator<IDataSource, ItemChangeableList, DataGeneratorItemList.DataHolderItemList> {

    @Override
    public boolean canGenerateForSource(IDataSource source) {
        return getHandler(source) != null;
    }

    @Override
    public boolean canGenerateForData(IData data) {
        return data instanceof ItemChangeableList;
    }

    @Override
    public ItemChangeableList generateData(DataHolderItemList holder, ItemChangeableList data, IDataSource connection) {
        BlockConnection bConnection = (BlockConnection) connection;
        TileEntity tile = bConnection.coords.getTileEntity();
        if(tile == null){
            return holder.getData(); // add an error?
        }
        List<StoredItemStack> info = new ArrayList<>();
        StorageSize size = holder.handler.getItems(info, tile, bConnection.face);
        data.sizing.add(size);
        for (StoredItemStack item : info) {
            data.add(item);
        }
        return data;
    }

    @Nullable
    public ISonarInventoryHandler getHandler(IDataSource connection){
        if(connection instanceof BlockConnection) {
            BlockConnection bConnection = (BlockConnection) connection;
            TileEntity tile = bConnection.coords.getTileEntity();
            if (tile == null) {
                return null;
            }
            for (ISonarInventoryHandler provider : SonarCore.inventoryHandlers) {
                if (provider.canHandleItems(tile, bConnection.face)) {
                    return provider;
                }
            }
        }
        return null;
    }

    public static class DataHolderItemList extends DataHolder<ItemChangeableList, DataHolderItemList> {

        private final ISonarInventoryHandler handler;

        public DataHolderItemList(IDataGenerator generator, ISonarInventoryHandler handler, IDataSource source, ItemChangeableList freshData, int tickRate){
            super(generator, source, freshData, tickRate);
            this.handler = handler;
        }
    }

}