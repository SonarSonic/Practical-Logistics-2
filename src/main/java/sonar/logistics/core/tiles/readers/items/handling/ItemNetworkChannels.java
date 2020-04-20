package sonar.logistics.core.tiles.readers.items.handling;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.utils.Pair;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListHandler;
import sonar.logistics.base.channels.handling.ListNetworkChannels;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemNetworkChannels extends ListNetworkChannels<MonitoredItemStack, INetworkListHandler<MonitoredItemStack, ItemChangeableList>> {

	public boolean updateLargeInventory = false;
	public List<ItemStack> forRapidUpdate = new ArrayList<>();

	public ItemNetworkChannels(ILogisticsNetwork network) {
		super(ItemNetworkHandler.INSTANCE, network);
	}

	public boolean updateLargeInventory() {
		return updateLargeInventory;
	}

	public void updateChannel() {
		//as every reader is updated during the rapid update there is no need to update the queued channels/readers  
		if (forRapidUpdate.isEmpty()) {
			super.updateChannel();
		} else {
			performRapidUpdates();//rapid updates do not do full lists
		}
	}

	public void updateTickLists() {
		super.updateTickLists();
		updateLargeInventory = false;
		for (IListReader reader : readers) {
			if (!reader.getListenerList().getAllListeners(ListenerType.NEW_GUI_LISTENER, ListenerType.NEW_DISPLAY_LISTENER, ListenerType.TEMPORARY_LISTENER).isEmpty()) {
				updateLargeInventory = true;
				break;
			}
		}
	}

	public void performRapidUpdates() {
		updateAllChannels();
		for (IListReader reader : readers) {
			Pair<InfoUUID, AbstractChangeableList<MonitoredItemStack>> updateList = handler.updateAndSendList(network, reader, channels, false);
			InfoPacketHelper.sendRapidItemUpdate(reader, updateList.a, (ItemChangeableList) updateList.b, forRapidUpdate);
		}
		forRapidUpdate.clear();
	}

	public void createRapidItemUpdate(List<ItemStack> items) {
		newItems: for (ItemStack stack : items) {
			for (ItemStack stored : forRapidUpdate) {
				if (StoredItemStack.isEqualStack(stored, stack)) {
					continue newItems;
				}
			}
			forRapidUpdate.add(stack.copy());
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
