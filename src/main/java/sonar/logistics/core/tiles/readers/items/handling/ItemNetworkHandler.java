package sonar.logistics.core.tiles.readers.items.handling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.ISonarInventoryHandler;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.inventory.handling.ItemTransferHelper;
import sonar.logistics.PL2Config;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.channels.IEntityMonitorHandler;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListChannels;
import sonar.logistics.api.core.tiles.readers.channels.ITileMonitorHandler;
import sonar.logistics.api.core.tiles.wireless.emitters.IDataEmitter;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.handling.ListNetworkHandler;
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
		List<ISonarInventoryHandler> providers = SonarCore.inventoryHandlers;
		TileEntity tile = connection.coords.getTileEntity();
		if (tile != null) {
			for (ISonarInventoryHandler provider : providers) {
				if (provider.canHandleItems(tile, connection.face)) {
					if (!provider.isLargeInventory() || channels.updateLargeInventory()) {
						List<StoredItemStack> info = new ArrayList<>();
						StorageSize size = provider.getItems(info, tile, connection.face);
						itemList.sizing.add(size);
						for (StoredItemStack item : info) {
							itemList.add(item);
						}
					}
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
