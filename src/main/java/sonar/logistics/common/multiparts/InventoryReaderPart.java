package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.SonarMultipartInventory;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.Pair;
import sonar.core.utils.SortingDirection;
import sonar.logistics.PL2;
import sonar.logistics.PL2Items;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.filters.IFilteredTile;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.readers.InventoryReader;
import sonar.logistics.api.readers.InventoryReader.Modes;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.client.gui.GuiInventoryReader;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.client.gui.generic.GuiFilterList;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerFilterList;
import sonar.logistics.common.containers.ContainerInventoryReader;
import sonar.logistics.connections.monitoring.ItemMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.ItemHelper;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.ProgressInfo;
import sonar.logistics.network.sync.SyncFilterList;

public class InventoryReaderPart extends ReaderMultipart<MonitoredItemStack> implements IByteBufTile, IFilteredTile {

	public SonarMultipartInventory inventory = new SonarMultipartInventory(this, 1);
	public SyncEnum<InventoryReader.Modes> setting = (SyncEnum) new SyncEnum(InventoryReader.Modes.values(), 2).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT targetSlot = (INT) new SyncTagType.INT(3).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT posSlot = (INT) new SyncTagType.INT(4).addSyncType(SyncType.SPECIAL);
	public SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 5).addSyncType(SyncType.SPECIAL);
	public SyncEnum<InventoryReader.SortingType> sortingType = (SyncEnum) new SyncEnum(InventoryReader.SortingType.values(), 6).addSyncType(SyncType.SPECIAL);
	public SyncFilterList filters = new SyncFilterList(9);
	
	{
		syncList.addParts(inventory, setting, targetSlot, posSlot, sortingOrder, sortingType, filters);
	}

	public InventoryReaderPart() {
		super(ItemMonitorHandler.id);
	}

	public InventoryReaderPart(EnumFacing face) {
		super(ItemMonitorHandler.id, face);
	}

	//// ILogicReader \\\\

	@Override
	public MonitoredList<MonitoredItemStack> sortMonitoredList(MonitoredList<MonitoredItemStack> updateInfo, int channelID) {		
		ItemHelper.sortItemList(updateInfo, sortingOrder.getObject(), sortingType.getObject());
		return updateInfo;
	}


	public boolean canMonitorInfo(MonitoredItemStack info, int infoID, Map<NodeConnection, MonitoredList<?>> channels, ArrayList<NodeConnection> usedChannels) {
		if(this.setting.getObject()==Modes.FILTERED){
			return filters.matches(info.getStoredStack(), NodeTransferMode.ADD_REMOVE);
		}		
		return true;
	}
	@Override
	public void setMonitoredInfo(MonitoredList<MonitoredItemStack> updateInfo, ArrayList<NodeConnection> usedChannels, int channelID) {
		IMonitorInfo info = null;
		switch (setting.getObject()) {
		case INVENTORIES:
		case FILTERED:
			info = new LogicInfoList(getIdentity(), MonitoredItemStack.id, this.getNetworkID());
			break;
		case POS:
			int pos = posSlot.getObject();
			if (pos < updateInfo.size())
				info = updateInfo.get(posSlot.getObject());
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
				newInfo.networkID.setObject(network.getNetworkID());
				info = newInfo;
			}
			// make a way of getting the stack
			break;
		case STACK:
			ItemStack stack = inventory.getStackInSlot(0);
			if (stack != null) {
				MonitoredItemStack dummyInfo = new MonitoredItemStack(new StoredItemStack(stack.copy(), 0), network.getNetworkID());
				Pair<Boolean, IMonitorInfo> latestInfo = updateInfo.getLatestInfo(dummyInfo);
				if (latestInfo.b instanceof MonitoredItemStack) {
					((MonitoredItemStack) latestInfo.b).networkID.setObject(network.getNetworkID());
				}
				info = latestInfo.a ? latestInfo.b : dummyInfo;
			}
			break;
		case STORAGE:
			info = new ProgressInfo(LogicInfo.buildDirectInfo("item.storage", RegistryType.TILE, updateInfo.sizing.getStored()), LogicInfo.buildDirectInfo("max", RegistryType.TILE, updateInfo.sizing.getMaxStored()));
			break;
		default:
			break;
		}
		if (info != null) {
			InfoUUID id = new InfoUUID(getIdentity().hashCode(), 0);
			IMonitorInfo oldInfo = PL2.getServerManager().info.get(id);
			if (oldInfo == null || !oldInfo.isMatchingType(info) || !oldInfo.isMatchingInfo(info) || !oldInfo.isIdenticalInfo(info)) {
				PL2.getServerManager().changeInfo(id, info);
			}
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

		// when the order of the list is changed the viewers need to recieve a full update
		if (id == 5 || id == 6) {
			ArrayList<EntityPlayer> players = viewers.getViewers(true, ViewerType.INFO);
			for (EntityPlayer player : players) {
				viewers.addViewer(player, ViewerType.TEMPORARY);
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
		}
		return null;
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
		}
		return null;
	}

	@Override
	public String getDisplayName() {
		return FontHelper.translate("item.InventoryReader.name");
	}

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(PL2Items.inventory_reader);
	}

	@Override
	public boolean allowed(ItemStack stack) {
		return filters.matches(new StoredItemStack(stack), NodeTransferMode.ADD_REMOVE);
	}

	@Override
	public SyncFilterList getFilters() {
		return filters;
	}

}