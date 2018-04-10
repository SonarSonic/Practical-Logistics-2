package sonar.logistics.common.multiparts.readers;

import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.SonarInventory;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.filters.IFilteredTile;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.ItemChangeableList;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.tiles.readers.InventoryReader;
import sonar.logistics.api.tiles.readers.InventoryReader.Modes;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.client.gui.GuiInventoryReader;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.client.gui.generic.GuiFilterList;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerFilterList;
import sonar.logistics.common.containers.ContainerInventoryReader;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.info.types.ProgressInfo;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.items.ItemHelper;
import sonar.logistics.networking.items.ItemNetworkChannels;
import sonar.logistics.networking.items.ItemNetworkHandler;
import sonar.logistics.packets.sync.SyncFilterList;

public class TileInventoryReader extends TileAbstractListReader<MonitoredItemStack> implements IByteBufTile, IFilteredTile {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK, TileMessage.NO_STACK_SELECTED };

	public SonarInventory inventory = new SonarInventory(this, 1);
	public SyncEnum<InventoryReader.Modes> setting = (SyncEnum) new SyncEnum(InventoryReader.Modes.values(), 2).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT targetSlot = (INT) new SyncTagType.INT(3).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT posSlot = (INT) new SyncTagType.INT(4).addSyncType(SyncType.SPECIAL);
	public SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 5).addSyncType(SyncType.SPECIAL);
	public SyncEnum<InventoryReader.SortingType> sortingType = (SyncEnum) new SyncEnum(InventoryReader.SortingType.values(), 6).addSyncType(SyncType.SPECIAL);
	public SyncFilterList filters = new SyncFilterList(9);

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
		return ItemHelper.sortItemList(updateInfo, sortingOrder.getObject(), sortingType.getObject());
	}

	public boolean canMonitorInfo(IMonitoredValue<MonitoredItemStack> info, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<MonitoredItemStack>> channels, List<NodeConnection> usedChannels) {
		if (this.setting.getObject() == Modes.FILTERED) {
			return filters.matches(info.getSaveableInfo().getStoredStack(), NodeTransferMode.ADD_REMOVE);
		}
		return true;
	}

	@Override
	public void setMonitoredInfo(AbstractChangeableList<MonitoredItemStack> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		IInfo info = null;
		switch (setting.getObject()) {
		case INVENTORIES:
		case FILTERED:
			info = new LogicInfoList(getIdentity(), MonitoredItemStack.id, this.getNetworkID());
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
			if (stack != null) {
				MonitoredItemStack dummyInfo = new MonitoredItemStack(new StoredItemStack(stack.copy(), 0), network.getNetworkID());
				IMonitoredValue<MonitoredItemStack> value = updateInfo.find(dummyInfo);				
				info = value == null ? dummyInfo : new MonitoredItemStack(value.getSaveableInfo().getStoredStack().copy(), network.getNetworkID()); // FIXME should check EnumlistChange
			}
			break;
		case STORAGE:
			StorageSize size = updateInfo instanceof ItemChangeableList ? ((ItemChangeableList) updateInfo).sizing : new StorageSize(0, 0);
			info = new ProgressInfo(LogicInfo.buildDirectInfo("item.storage", RegistryType.TILE, size.getStored()), LogicInfo.buildDirectInfo("max", RegistryType.TILE, size.getMaxStored()));
			break;
		default:
			break;
		}
		ServerInfoHandler.instance().changeInfo(this, uuid, info);
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
		// when the order of the list is changed the viewers need to recieve a full update
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
	public boolean allowed(ItemStack stack) {
		return filters.matches(new StoredItemStack(stack), NodeTransferMode.ADD_REMOVE);
	}

	@Override
	public SyncFilterList getFilters() {
		return filters;
	}

	@Override
	public TileMessage[] getValidMessages() {
		return validStates;
	}

}