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
import sonar.logistics.PL2ASMLoader;
import sonar.logistics.PL2Config;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.EntityMonitorHandler;
import sonar.logistics.api.asm.NetworkHandler;
import sonar.logistics.api.asm.NetworkHandlerField;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.common.multiparts.wireless.DataEmitterPart;
import sonar.logistics.connections.channels.InfoNetworkChannels;
import sonar.logistics.connections.channels.ItemNetworkChannels;
import sonar.logistics.info.types.MonitoredItemStack;

@EntityMonitorHandler(handlerID = ItemNetworkHandler.id, modid = PL2Constants.MODID)
@NetworkHandler(handlerID = ItemNetworkHandler.id, modid = PL2Constants.MODID)
public class ItemNetworkHandler extends ListNetworkHandler<MonitoredItemStack> implements ITileMonitorHandler<MonitoredItemStack, ItemNetworkChannels>, IEntityMonitorHandler<MonitoredItemStack, ItemNetworkChannels> {

	@NetworkHandlerField(handlerID = ItemNetworkHandler.id)
	public static ItemNetworkHandler INSTANCE;

	public static final String id = "item";

	@Override
	public String id() {
		return id;
	}

	public int getReaderID(IListReader reader) {
		if (reader instanceof IDataEmitter) {
			return DataEmitterPart.STATIC_ITEM_ID;
		}
		return 0;
	}

	@Override
	public ItemNetworkChannels instance(ILogisticsNetwork network) {
		return new ItemNetworkChannels(INSTANCE, network);
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
