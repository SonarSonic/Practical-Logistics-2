package sonar.logistics.base.data.convertors;
/*
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.data.DataManager;
import sonar.logistics.base.data.api.IDataWatcher;
import sonar.logistics.base.data.sources.MultiDataSource;
import sonar.logistics.core.tiles.displays.info.types.InfoError;
import sonar.logistics.core.tiles.displays.info.types.LogicInfoList;
import sonar.logistics.core.tiles.displays.info.types.general.LogicInfo;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;
import sonar.logistics.core.tiles.displays.info.types.progress.InfoProgressBar;
import sonar.logistics.core.tiles.readers.items.TileInventoryReader;
import sonar.logistics.core.tiles.readers.items.handling.ItemHelper;
import sonar.logistics.core.tiles.readers.items.handling.ItemNetworkChannels;

import java.util.List;

public abstract class UpdaterInventoryReader extends AbstractDataConvertor<TileInventoryReader> {

    public UpdaterInventoryReader(InfoUUID uuid, TileInventoryReader source){
        super(uuid, source);
    }
    public ItemChangeableList getUUIDLatestList(InfoUUID uuid){
        return (ItemChangeableList)ServerInfoHandler.instance().getChangeableListMap().compute(uuid, (v, l) -> {
            if(!(l instanceof ItemChangeableList)) {
               //FIXME ServerInfoHandler.instance().markChanged();
                return ItemChangeableList.newChangeableList();
            }
            return l;
        });
    }

    public ItemNetworkChannels getChannel(){
        return (ItemNetworkChannels) getSourceTile().getNetworkChannels();
    }

    public static class ItemList extends UpdaterInventoryReader implements IDataWatcher {

        public MultiDataSource source = new MultiDataSource(getSourceTile().getChannels(), getSourceTile());
        public IDataHolder<ItemChangeableList> list = DataManager.instance().getOrCreateDataHolder(source, () -> ItemChangeableList.newChangeableList(), 20);
        public List<IDataHolder> holders = Lists.newArrayList(list);

        public ItemList(InfoUUID uuid, TileInventoryReader source){
            super(uuid, source);
        }

        @Override
        public IInfo update(IInfo current) {
            if(current instanceof LogicInfoList && getSourceTile().sorting_changed){
                ServerInfoHandler.instance().markChanged(getSourceTile(), getUUID());
                getSourceTile().sorting_changed = false;
                return current;
            }else{
                LogicInfoList list = new LogicInfoList(getSourceTile().getIdentity(), MonitoredItemStack.id, getSourceTile().getNetworkID());
                list.listSorter = getSourceTile().inventory_sorter;
                return list;
            }
        }

        @Override
        public List<IDataHolder> getDataHolders() {
            return holders;
        }
    }

    public static class ItemListIndex extends UpdaterInventoryReader{

        //public SyncTagType.INT posSlot = (SyncTagType.INT) new SyncTagType.INT(4).addSyncType(NBTHelper.SyncType.SPECIAL);

        public ItemListIndex(InfoUUID uuid, TileInventoryReader source){
            super(uuid, source);
        }

        @Override
        public IInfo update(IInfo current) {
            ItemChangeableList list = getUUIDLatestList(getUUID());
            int pos = getSourceTile().posSlot.getObject();
            if (pos < list.getValueCount()) {
                MonitoredItemStack posItem = list.getActualValue(pos).copy();
                posItem.setNetworkSource(getSourceTile().network.getNetworkID());
                return posItem;
            }
            return InfoError.noItem;
        }

        @Override
        public List<IDataHolder> getDataHolders() {
            return null;
        }
    }

    public static class InventorySlot extends UpdaterInventoryReader{

        //public SyncTagType.INT targetSlot = (SyncTagType.INT) new SyncTagType.INT(3).addSyncType(NBTHelper.SyncType.SPECIAL);

        public InventorySlot(InfoUUID uuid, TileInventoryReader source){
            super(uuid, source);
        }

        @Override
        public IInfo update(IInfo current) {
            ItemNetworkChannels channel = getChannel();
            List<NodeConnection> channels =  channel.usedChannels.get(getSourceTile().getIdentity());
            StoredItemStack slotStack = null;
            if (!channels.isEmpty()) {
                NodeConnection connection = channels.get(0);
                if (connection != null) {
                    if (connection instanceof BlockConnection) {
                        slotStack = ItemHelper.getTileStack((BlockConnection) connection, getSourceTile().targetSlot.getObject());
                    }
                    if (connection instanceof EntityConnection) {
                        slotStack = ItemHelper.getEntityStack((EntityConnection) connection, getSourceTile().targetSlot.getObject());
                    }
                }
            }
            if (slotStack != null) {
                MonitoredItemStack newInfo = new MonitoredItemStack(slotStack);
                newInfo.setNetworkSource(getSourceTile().network.getNetworkID());
                return newInfo;
            }
            return InfoError.noItem;
        }

        @Override
        public List<IDataHolder> getDataHolders() {
            return null;
        }
    }

    public static class SelectedStack extends UpdaterInventoryReader {

        public SelectedStack(InfoUUID uuid, TileInventoryReader source) {
            super(uuid, source);
        }

        @Override
        public IInfo update(IInfo current) {
            ItemChangeableList list = getUUIDLatestList(getUUID());
            ItemStack stack = getSourceTile().inventory.getStackInSlot(0);
            if (!stack.isEmpty()) {
                MonitoredItemStack dummyInfo = new MonitoredItemStack(new StoredItemStack(stack.copy(), 0), getSourceTile().network.getNetworkID());
                IMonitoredValue<MonitoredItemStack> value = list.find(dummyInfo);
                return value == null ? dummyInfo : new MonitoredItemStack(value.getSaveableInfo().getStoredStack().copy(), getSourceTile().network.getNetworkID()); // FIXME should check EnumlistChange
            }else{
                return new InfoError("NO ITEM SELECTED");
            }
        }

        @Override
        public List<IDataHolder> getDataHolders() {
            return null;
        }
    }

    public static class Storage extends UpdaterInventoryReader {

        public Storage(InfoUUID uuid, TileInventoryReader source) {
            super(uuid, source);
        }

        @Override
        public IInfo update(IInfo current) {
            ItemChangeableList list = getUUIDLatestList(getUUID());
            LogicInfo stored = LogicInfo.buildDirectInfo("item.storage", RegistryType.TILE, list.sizing.getStored());
            LogicInfo max = LogicInfo.buildDirectInfo("max", RegistryType.TILE, list.sizing.getMaxStored());
            return new InfoProgressBar(stored, max);
        }

        @Override
        public List<IDataHolder> getDataHolders() {
            return null;
        }
    }


}
*/