package sonar.logistics.base.data.generators;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.data.api.IData;
import sonar.logistics.base.data.api.IDataGenerator;
import sonar.logistics.base.data.api.IDataHolder;
import sonar.logistics.base.data.generators.items.ITileInventoryProvider;
import sonar.logistics.base.data.sources.IDataSource;
import sonar.logistics.core.tiles.displays.info.MasterInfoRegistry;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;

public class DataGeneratorItemList implements IDataGenerator<IDataSource, ItemChangeableList, IDataHolder<ItemChangeableList>> {

    @Override
    public boolean canGenerateForSource(IDataSource source) {
        if(source instanceof BlockConnection) {
            BlockConnection bConnection = (BlockConnection) source;
            TileEntity tile = bConnection.coords.getTileEntity();
            if(tile != null) {
                for (ITileInventoryProvider provider : MasterInfoRegistry.INSTANCE.inventoryProviders) {
                    IItemHandler handler = provider.getHandler(tile, bConnection.face);
                    if (handler != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canGenerateForData(IData data) {
        return data instanceof ItemChangeableList;
    }

    @Override
    public ItemChangeableList generateData(IDataHolder<ItemChangeableList> holder, ItemChangeableList data, IDataSource connection) {
        if(connection instanceof BlockConnection) {
            BlockConnection bConnection = (BlockConnection) connection;
            TileEntity tile = bConnection.coords.getTileEntity();
            if(tile != null) {
                for (ITileInventoryProvider provider : MasterInfoRegistry.INSTANCE.inventoryProviders) {
                    IItemHandler handler = provider.getHandler(tile, bConnection.face);
                    if (handler != null) {
                        provider.getItemList(data, handler, tile, bConnection.face);
                        break;
                    }
                }
            }
        }
        return data;
    }

}