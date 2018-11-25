package sonar.logistics.core.tiles.readers.items;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.handlers.inventories.SonarInventory;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;
import sonar.logistics.api.core.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.*;
import sonar.logistics.base.filters.ContainerFilterList;
import sonar.logistics.base.filters.GuiFilterList;
import sonar.logistics.base.filters.IFilteredTile;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.core.tiles.displays.info.types.InfoError;
import sonar.logistics.core.tiles.displays.info.types.LogicInfoList;
import sonar.logistics.core.tiles.displays.info.types.general.LogicInfo;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;
import sonar.logistics.core.tiles.displays.info.types.progress.InfoProgressBar;
import sonar.logistics.core.tiles.readers.base.TileAbstractListReader;
import sonar.logistics.core.tiles.readers.items.InventoryReader.Modes;
import sonar.logistics.core.tiles.readers.items.InventoryReader.SortingType;
import sonar.logistics.core.tiles.readers.items.handling.ItemHelper;
import sonar.logistics.core.tiles.readers.items.handling.ItemNetworkChannels;
import sonar.logistics.core.tiles.readers.items.handling.ItemNetworkHandler;
import sonar.logistics.network.sync.SyncFilterList;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TileInventoryReader extends TileAbstractListReader<MonitoredItemStack> implements IByteBufTile, IFilteredTile {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK, ErrorMessage.NO_STACK_SELECTED };

	public SonarInventory inventory = new SonarInventory(1);
	public SyncEnum<InventoryReader.Modes> setting = (SyncEnum) new SyncEnum(InventoryReader.Modes.values(), 2).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT targetSlot = (INT) new SyncTagType.INT(3).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT posSlot = (INT) new SyncTagType.INT(4).addSyncType(SyncType.SPECIAL);
	public SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 5).addSyncType(SyncType.SPECIAL);
	public SyncEnum<InventoryReader.SortingType> sortingType = (SyncEnum) new SyncEnum(InventoryReader.SortingType.values(), 6).addSyncType(SyncType.SPECIAL);
	public SyncFilterList filters = new SyncFilterList(9);
	public InventorySorter inventory_sorter = new InventorySorter(){
		
		public SortingDirection getDirection(){
			return sortingOrder.getObject();
		}
		
		public SortingType getType(){
			return sortingType.getObject();
		}
	};
	public boolean sorting_changed = true;

	{
		syncList.addParts(inventory, setting, targetSlot, posSlot, sortingOrder, sortingType, filters);
	}

	@Override
	public List<INetworkHandler> addValidHandlers(List<INetworkHandler> handlers) {
		handlers.add(ItemNetworkHandler.INSTANCE);
		return handlers;
	}

	//// ILogicReader \\\\

	@Override
	public int getMaxInfo() {
		return 1;
	}
	
	@Override
	public AbstractChangeableList<MonitoredItemStack> getViewableList(AbstractChangeableList<MonitoredItemStack> updateList, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<MonitoredItemStack>> channels, List<NodeConnection> usedChannels) {
		if (updateList instanceof ItemChangeableList) {
			channels.values().forEach(list -> {
				if (list instanceof ItemChangeableList) {
					((ItemChangeableList) updateList).sizing.add(((ItemChangeableList) list).sizing);
				}
			});
		}
		return super.getViewableList(updateList, uuid, channels, usedChannels);
	}

	@Override
	public AbstractChangeableList<MonitoredItemStack> sortMonitoredList(AbstractChangeableList<MonitoredItemStack> updateInfo, int channelID) {
		return inventory_sorter.sortSaveableList(updateInfo);
	}

	public boolean canMonitorInfo(IMonitoredValue<MonitoredItemStack> info, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<MonitoredItemStack>> channels, List<NodeConnection> usedChannels) {
		if (this.setting.getObject() == Modes.FILTERED) {
			return filters.matches(info.getSaveableInfo().getStoredStack().getFullStack(), NodeTransferMode.ADD_REMOVE);
		}
		return true;
	}

	@Override
	public void setMonitoredInfo(AbstractChangeableList<MonitoredItemStack> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		IInfo info = null;
		switch (setting.getObject()) {
		case INVENTORIES:
		case FILTERED:
			LogicInfoList list = new LogicInfoList(getIdentity(), MonitoredItemStack.id, this.getNetworkID());
			list.listSorter = inventory_sorter;
			info = list;
			break;
		case POS:
			int pos = posSlot.getObject();
			if (pos < updateInfo.getValueCount()) {
				MonitoredItemStack posItem = updateInfo.getActualValue(pos).copy();
				posItem.setNetworkSource(network.getNetworkID());
				info = posItem;
			}
			break;
		case SLOT:
			StoredItemStack slotStack = null;
			if (!usedChannels.isEmpty()) {
				NodeConnection connection = usedChannels.get(0);
				if (connection != null) {
					if (connection instanceof BlockConnection) {
						slotStack = ItemHelper.getTileStack((BlockConnection) connection, targetSlot.getObject());
					}
					if (connection instanceof EntityConnection) {
						slotStack = ItemHelper.getEntityStack((EntityConnection) connection, targetSlot.getObject());
					}

				}
			}
			if (slotStack != null) {
				MonitoredItemStack newInfo = new MonitoredItemStack(slotStack);
				newInfo.setNetworkSource(network.getNetworkID());
				info = newInfo;
			}
			// make a way of getting the stack
			break;
		case STACK:
			ItemStack stack = inventory.getStackInSlot(0);
			if (!stack.isEmpty()) {
				MonitoredItemStack dummyInfo = new MonitoredItemStack(new StoredItemStack(stack.copy(), 0), network.getNetworkID());
				IMonitoredValue<MonitoredItemStack> value = updateInfo.find(dummyInfo);				
				info = value == null ? dummyInfo : new MonitoredItemStack(value.getSaveableInfo().getStoredStack().copy(), network.getNetworkID()); // FIXME should check EnumlistChange
			}else{
				info = new InfoError("NO ITEM SELECTED");
			}
			break;
		case STORAGE:
			StorageSize size = updateInfo instanceof ItemChangeableList ? ((ItemChangeableList) updateInfo).sizing : new StorageSize(0, 0);
			info = new InfoProgressBar(LogicInfo.buildDirectInfo("items.storage", RegistryType.TILE, size.getStored()), LogicInfo.buildDirectInfo("max", RegistryType.TILE, size.getMaxStored()));
			break;
		default:
			break;
		}
		ServerInfoHandler.instance().changeInfo(this, uuid, info);

		if (sorting_changed) {
			ServerInfoHandler.instance().markChanged(this, uuid);
			sorting_changed = false;
		}
	}

	//// IChannelledTile \\\\

	@Override
	public ChannelType channelType() {
		return ChannelType.UNLIMITED;
	}

	//// PACKETS \\\\

	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
	}

	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		if (id == sortingOrder.id || id == sortingType.id) {
			sorting_changed = true;
		}
		// when the order of the list is changed the listeners need to recieve a full update
		if (id == 5 || id == 6) {
			ItemNetworkChannels list = network.getNetworkChannels(ItemNetworkChannels.class);
			if (list != null) {
				List<PlayerListener> players = listeners.getListeners(ListenerType.OLD_GUI_LISTENER);
				players.forEach(player -> list.sendLocalRapidUpdate(this, player.player));
			}
		}
	}

	//// GUI \\\\

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerInventoryReader(this, player);
		case 1:
			return new ContainerChannelSelection(this);
		case 2:
			return new ContainerFilterList(player, this);
		default:
			return null;
		}
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiInventoryReader(this, player);
		case 1:
			return new GuiChannelSelection(player, this, 0);
		case 2:
			return new GuiFilterList(player, this, 0);
		default:
			return null;
		}
	}

	@Override
	public SyncFilterList getFilters() {
		return filters;
	}

	@Override
	public Predicate<ItemStack> getFilter() {
		return s -> filters.matches(s, NodeTransferMode.ADD_REMOVE);
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public ILogicListSorter getSorter() {
		return inventory_sorter;
	}

}