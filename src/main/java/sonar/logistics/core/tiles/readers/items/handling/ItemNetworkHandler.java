package sonar.logistics.core.tiles.readers.items.handling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.handlers.inventories.handling.ItemTransferHelper;
import sonar.logistics.PL2Config;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.channels.IEntityMonitorHandler;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListChannels;
import sonar.logistics.api.core.tiles.readers.channels.ITileMonitorHandler;
import sonar.logistics.api.core.tiles.wireless.emitters.IDataEmitter;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.handling.ListNetworkHandler;
import sonar.logistics.base.data.generators.items.ITileInventoryProvider;
import sonar.logistics.core.tiles.displays.info.MasterInfoRegistry;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;
import sonar.logistics.core.tiles.wireless.emitters.TileDataEmitter;

import java.util.ArrayList;
import java.util.List;

public class ItemNetworkHandler extends ListNetworkHandler<MonitoredItemStack, ItemChangeableList> implements ITileMonitorHandler<MonitoredItemStack, ItemChangeableList, ItemNetworkChannels>, IEntityMonitorHandler<MonitoredItemStack, ItemChangeableList, ItemNetworkChannels> {

	public static ItemNetworkHandler INSTANCE = new ItemNetworkHandler();

	public int getReaderID(IListReader reader) {
		if (reader instanceof IDataEmitter) {
			return TileDataEmitter.STATIC_ITEM_ID;
		}
		return 0;
	}

	@Override
	public Class<? extends INetworkListChannels> getChannelsType(){
		return ItemNetworkChannels.class;
	}

	@Override
	public ItemChangeableList updateInfo(ItemNetworkChannels channels, ItemChangeableList itemList, BlockConnection connection) {
		TileEntity tile = connection.coords.getTileEntity();
		if (tile != null) {
			for (ITileInventoryProvider provider : MasterInfoRegistry.INSTANCE.inventoryProviders) {
				IItemHandler handler = provider.getHandler(tile, connection.face);
				if(handler != null){
					provider.getItemList(itemList, handler, tile, connection.face);
					break;
				}
			}
		}
		return itemList;
	}

	@Override
	public ItemChangeableList updateInfo(ItemNetworkChannels channels, ItemChangeableList itemList, EntityConnection connection) {
		Entity entity = connection.entity;
		if (entity instanceof EntityPlayer) {
			List<StoredItemStack> info = new ArrayList<>();
			StorageSize size = ItemTransferHelper.addInventoryToList(info, ((EntityPlayer) entity).inventory);
			itemList.sizing.add(size);
			for (StoredItemStack item : info) {
				itemList.add(item);
			}
		}
		return itemList;
	}

	@Override
	public int updateRate() {
		return PL2Config.inventoryUpdate;
	}

	@Override
	public ItemChangeableList newChangeableList() {
		return new ItemChangeableList();
	}
}
