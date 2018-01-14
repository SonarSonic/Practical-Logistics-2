package sonar.logistics.networking.handlers;

import java.util.List;
import java.util.Map;

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
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.ItemChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.common.multiparts.wireless.TileDataEmitter;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.networking.channels.ItemNetworkChannels;

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
	public ItemChangeableList updateInfo(ItemNetworkChannels channels, ItemChangeableList newList, BlockConnection connection) {
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
							newList.add(item);
						}
					} else {
						//return previousList; //FIXME NEEDS TO RESTORE STATE??? - OR SET EVERYTHING TO EQUAL, RATHER THAN OLD, SO THEY WON'T BE DELETED
					}
					break;
				}
			}
		}
		return newList;
	}

	@Override
	public ItemChangeableList updateInfo(ItemNetworkChannels channels, ItemChangeableList newList, EntityConnection connection) {
		Entity entity = connection.entity;
		if (entity instanceof EntityPlayer) {
			List<StoredItemStack> info = Lists.newArrayList();
			StorageSize size = GenericInventoryHandler.getItems(info, ((EntityPlayer) entity).inventory, null);
			newList.sizing.add(size);
			for (StoredItemStack item : info) {
				newList.add(item);
			}
		}
		return newList;
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
