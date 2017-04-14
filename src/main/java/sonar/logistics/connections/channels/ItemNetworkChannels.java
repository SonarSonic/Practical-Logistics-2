package sonar.logistics.connections.channels;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.connections.handlers.ItemNetworkHandler;
import sonar.logistics.info.types.MonitoredItemStack;

//TODO create custom version for Inventory Reader which will do one slot at a time at low numbers.
public class ItemNetworkChannels extends ListNetworkChannels<MonitoredItemStack, INetworkListHandler<MonitoredItemStack>> {

	public boolean updateLargeInventory = false;

	public ItemNetworkChannels(ItemNetworkHandler handler, ILogisticsNetwork network) {
		super(handler, network);
	}

	public boolean updateLargeInventory() {
		return updateLargeInventory; // TODO THIS IS NO WORKING WELL
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
