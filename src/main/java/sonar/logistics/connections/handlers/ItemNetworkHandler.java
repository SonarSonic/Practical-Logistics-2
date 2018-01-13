package sonar.logistics.connections.handlers;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.ISonarInventoryHandler;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.inventory.GenericInventoryHandler;
import sonar.logistics.PL2Config;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.common.multiparts2.wireless.TileDataEmitter;
import sonar.logistics.connections.channels.ItemNetworkChannels;
import sonar.logistics.info.types.MonitoredItemStack;

public class ItemNetworkHandler extends ListNetworkHandler<MonitoredItemStack> implements ITileMonitorHandler<MonitoredItemStack, ItemNetworkChannels>, IEntityMonitorHandler<MonitoredItemStack, ItemNetworkChannels> {

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
	public MonitoredList<MonitoredItemStack> updateInfo(ItemNetworkChannels channels, MonitoredList<MonitoredItemStack> newList, MonitoredList<MonitoredItemStack> previousList, BlockConnection connection) {
		List<ISonarInventoryHandler> providers = SonarCore.inventoryHandlers;
		TileEntity tile = connection.coords.getTileEntity();
		if (tile != null) {
			for (ISonarInventoryHandler provider : providers) {
				if (provider.canHandleItems(tile, connection.face)) {
					if (!provider.isLargeInventory() || channels.updateLargeInventory()) {
						List<StoredItemStack> info = Lists.newArrayList();
						StorageSize size = provider.getItems(info, tile, connection.face);
						newList.sizing.add(size);

						for (StoredItemStack item : info) {
							newList.addInfoToList(new MonitoredItemStack(item), previousList);
						}
					} else {
						return previousList;
					}
					break;
				}
			}
		}
		return newList;
	}

	@Override
	public MonitoredList<MonitoredItemStack> updateInfo(ItemNetworkChannels channels, MonitoredList<MonitoredItemStack> newList, MonitoredList<MonitoredItemStack> previousList, EntityConnection connection) {
		Entity entity = connection.entity;
		if (entity instanceof EntityPlayer) {
			List<StoredItemStack> info = Lists.newArrayList();
			StorageSize size = GenericInventoryHandler.getItems(info, ((EntityPlayer) entity).inventory, null);
			newList.sizing.add(size);
			for (StoredItemStack item : info) {
				newList.addInfoToList(new MonitoredItemStack(item), previousList);
			}
		}
		return newList;
	}

	@Override
	public int updateRate() {
		return PL2Config.inventoryUpdate;
	}
}
