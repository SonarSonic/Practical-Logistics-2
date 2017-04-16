package sonar.logistics.connections.channels;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.connections.handlers.ItemNetworkHandler;
import sonar.logistics.info.types.MonitoredItemStack;

public class ItemNetworkChannels extends ListNetworkChannels<MonitoredItemStack, INetworkListHandler<MonitoredItemStack>> {

	public boolean updateLargeInventory = false;

	public ItemNetworkChannels(ILogisticsNetwork network) {
		super(ItemNetworkHandler.INSTANCE, network);
	}

	public boolean updateLargeInventory() {
		return updateLargeInventory;
	}
	
	public void updateTickLists(){
		super.updateTickLists();
		updateLargeInventory = false;
		for (IListReader reader : readers) {
			if (!reader.getListenerList().getListeners(ListenerType.FULL_INFO, ListenerType.TEMPORARY).isEmpty()) {
				updateLargeInventory = true;
				break;
			}
		}
	}

	public void sendFullRapidUpdate() {
		updateLargeInventory = true;
		super.sendFullRapidUpdate();
		updateLargeInventory = false;
	}

	public void sendLocalRapidUpdate(IListReader<MonitoredItemStack> reader, EntityPlayer viewer) {
		updateLargeInventory = true;
		super.sendLocalRapidUpdate(reader, viewer);
		updateLargeInventory = false;
	}
}
