package sonar.logistics.networking.channels;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.ItemChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.networking.handlers.ItemNetworkHandler;

public class ItemNetworkChannels extends ListNetworkChannels<MonitoredItemStack, INetworkListHandler<MonitoredItemStack, ItemChangeableList>> {

	public boolean updateLargeInventory = false;
	public List<ItemStack> forRapidUpdate = Lists.newArrayList();

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
			if (!reader.getListenerList().getAllListeners(ListenerType.NEW_LISTENER, ListenerType.TEMP_LISTENER).isEmpty()) {
				updateLargeInventory = true;
				break;
			}
		}
	}

	public void performRapidUpdates() {
		updateAllChannels();
		for (IListReader reader : readers) {
			Pair<InfoUUID, AbstractChangeableList<MonitoredItemStack>> updateList = handler.updateAndSendList(network, reader, channels, false);
			PacketHelper.sendRapidItemUpdate(reader, updateList.a, (ItemChangeableList) updateList.b, forRapidUpdate);
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
