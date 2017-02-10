package sonar.logistics.connections.monitoring;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.ISonarInventoryHandler;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.inventory.GenericInventoryHandler;
import sonar.core.utils.SimpleProfiler;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.EntityMonitorHandler;
import sonar.logistics.api.asm.TileMonitorHandler;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.IEntityMonitorHandler;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.nodes.NodeConnection;

@EntityMonitorHandler(handlerID = ItemMonitorHandler.id, modid = Logistics.MODID)
@TileMonitorHandler(handlerID = ItemMonitorHandler.id, modid = Logistics.MODID)
public class ItemMonitorHandler extends LogicMonitorHandler<MonitoredItemStack> implements ITileMonitorHandler<MonitoredItemStack>, IEntityMonitorHandler<MonitoredItemStack> {

	public static final String id = "item";

	@Override
	public String id() {
		return id;
	}

	@Override
	public MonitoredList<MonitoredItemStack> updateInfo(INetworkCache network, MonitoredList<MonitoredItemStack> previousList, NodeConnection connection) {
		MonitoredList<MonitoredItemStack> list = MonitoredList.<MonitoredItemStack>newMonitoredList(network.getNetworkID());
		List<ISonarInventoryHandler> providers = SonarCore.inventoryHandlers;
		TileEntity tile = connection.coords.getTileEntity();
		if (tile != null) {
			for (ISonarInventoryHandler provider : providers) {
				if (provider.canHandleItems(tile, connection.face)) {
					List<StoredItemStack> info = new ArrayList();
					StorageSize size = provider.getItems(info, tile, connection.face);
					list.sizing.add(size);
					for (StoredItemStack item : info) {
						list.addInfoToList(new MonitoredItemStack(item), previousList);
					}
					break;
				}
			}
		}
		return list;
	}

	@Override
	public MonitoredList<MonitoredItemStack> updateInfo(INetworkCache network, MonitoredList<MonitoredItemStack> previousList, Entity entity) {
		MonitoredList<MonitoredItemStack> list = MonitoredList.<MonitoredItemStack>newMonitoredList(network.getNetworkID());
		if (entity instanceof EntityPlayer) {
			List<StoredItemStack> info = new ArrayList();
			StorageSize size = GenericInventoryHandler.getItems(info, ((EntityPlayer) entity).inventory, null);
			list.sizing.add(size);
			for (StoredItemStack item : info) {
				list.addInfoToList(new MonitoredItemStack(item), previousList);
			}
		}
		return list;
	}
}
