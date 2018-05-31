package sonar.logistics.core.tiles.readers.items.handling;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import sonar.core.SonarCore;
import sonar.core.api.SonarAPI;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.ISonarInventoryHandler;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;
import sonar.core.helpers.FontHelper;
import static sonar.core.inventory.handling.ItemTransferHelper.*;

import sonar.core.inventory.handling.ItemTransferHelper;
import sonar.core.network.PacketStackUpdate;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.readers.IWirelessStorageReader;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.core.tiles.connections.data.network.NetworkHelper;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ItemHelper {

	@Nonnull
	public static IItemHandler getNetworkItemHandler(ILogisticsNetwork network){
		return null;
	}

	public static void transferPlayerInventoryToNetwork(EntityPlayer player, ILogisticsNetwork network, Predicate<ItemStack> filter, int toTransfer){
		doSimpleTransfer(Lists.newArrayList(getMainInventoryHandler(player)), Lists.newArrayList(getNetworkItemHandler(network)), filter, toTransfer);
	}

	public static void transferNetworkInventoryToPlayer(EntityPlayer player, ILogisticsNetwork network, Predicate<ItemStack> filter, int toTransfer){
		doSimpleTransfer(Lists.newArrayList(getNetworkItemHandler(network)), Lists.newArrayList(getMainInventoryHandler(player)), filter, toTransfer);
	}

	public static ItemStack insertItemStack(ILogisticsNetwork network, ItemStack stack, int toTransfer){
		return ItemHandlerHelper.insertItem(getNetworkItemHandler(network), stack, false);
	}

	public static ItemStack extractItemStack(ILogisticsNetwork network, Predicate<ItemStack> filter, int toTransfer){
		return ItemTransferHelper.doExtract(Lists.newArrayList(getNetworkItemHandler(network)), filter, toTransfer);
	}

	public static StoredItemStack transferItems(ILogisticsNetwork network, StoredItemStack add, NodeTransferMode mode, ActionType action, Predicate<ItemStack> filter) {
		return NetworkHelper.forEachTileEntity(network, CacheType.ALL, c -> c.canTransferItem(c, add, mode), getTileAction(add, mode, action, filter)) ? add : null;
	}

	private static BiPredicate<BlockConnection, TileEntity> getTileAction(StoredItemStack stack, NodeTransferMode mode, ActionType action, Predicate<ItemStack> filter) {
		return (c, t) -> stack.setStackSize(transfer(mode, t, stack, c.face, action)).getStackSize() != 0;
	}

	private static StoredItemStack transfer(NodeTransferMode mode, TileEntity tile, StoredItemStack stack, EnumFacing dir, ActionType action) {
		List<ISonarInventoryHandler> handlers = SonarCore.inventoryHandlers;
		for (ISonarInventoryHandler handler : handlers) {
			if (handler.canHandleItems(tile, dir)) {
				StoredItemStack copy = stack.copy().setStackSize(stack);
				return stack = mode.shouldRemove() ? handler.removeStack(copy, tile, dir, action) : handler.addStack(stack, tile, dir, action);
			}
		}
		return null;
	}

	public static StorageSize getTileInventory(List<StoredItemStack> storedStacks, StorageSize storage, List<BlockConnection> connections) {
		for (BlockConnection entry : connections) {
			storage = getTileInventory(storedStacks, storage, entry);
		}
		return storage;
	}

	public static StorageSize getTileInventory(List<StoredItemStack> storedStacks, StorageSize storage, BlockConnection entry) {
		TileEntity tile = entry.coords.getTileEntity();
		if (tile == null) {
			return storage;
		}
		boolean specialProvider = false;
		for (ISonarInventoryHandler provider : SonarCore.inventoryHandlers)
			if (provider.canHandleItems(tile, entry.face) && !specialProvider) {
				StorageSize size = provider.getItems(storedStacks, tile, entry.face);
				if (size != StorageSize.EMPTY) {
					specialProvider = true;
					storage.add(size);
				}
			}
		return storage;
	}

	public static StorageSize getEntityInventory(List<StoredItemStack> storedStacks, StorageSize storage, List<Entity> entityList) {
		for (Entity entity : entityList) {
			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				StorageSize size = ItemTransferHelper.addInventoryToList(storedStacks, player.inventory);
				storage.add(size);
			}
		}
		return storage;

	}

	public static long getItemCount(ItemStack stack, ILogisticsNetwork network) {
		if (network.isValid()) {
			ItemNetworkChannels channels = network.getNetworkChannels(ItemNetworkChannels.class);
			channels.updateLargeInventory = true;
			channels.updateAllChannels();
			channels.updateLargeInventory = false;
			ItemChangeableList updateList = new ItemChangeableList();
			for (Entry<NodeConnection, AbstractChangeableList<MonitoredItemStack>> entry : channels.channels.entrySet()) {
				AbstractChangeableList<MonitoredItemStack> list = entry.getValue();
				if (!list.getList().isEmpty() && list instanceof ItemChangeableList) {
					for (IMonitoredValue<MonitoredItemStack> coordInfo : list.getList()) {
						updateList.add(coordInfo.getSaveableInfo().copy());
					}
				}
			}
			return updateList.getItemCount(stack);
		}
		return 0;

	}

	public static void addItemsFromPlayer(StoredItemStack add, EntityPlayer player, ILogisticsNetwork network, ActionType action, Predicate<ItemStack> filter) {
		IInventory inv = player.inventory;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getCount() != 0 && add.equalStack(stack)) {
				StoredItemStack toAdd = new StoredItemStack(stack.copy());
				StoredItemStack perform = transferItems(network, toAdd.copy(), NodeTransferMode.ADD, ActionType.PERFORM, filter);
				if (!toAdd.equals(perform)) {
					inv.setInventorySlotContents(i, StoredItemStack.getActualStack(perform));
					inv.markDirty();
				}
			}
		}
	}

	@Deprecated
	public static StoredItemStack getEntityStack(EntityConnection connection, int slot) {
		if (connection.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) connection.entity;
			IInventory inv = player.inventory;
			if (slot < inv.getSizeInventory()) {
				ItemStack stack = inv.getStackInSlot(slot);
				return new StoredItemStack(stack);
			}
		}
		return null;
	}

	@Deprecated
	public static StoredItemStack getTileStack(BlockConnection connection, int slot) {
		for (ISonarInventoryHandler provider : SonarCore.inventoryHandlers) {
			TileEntity tile = connection.coords.getTileEntity();
			if (tile != null && provider.canHandleItems(tile, connection.face)) {
				return provider.getStack(slot, tile, connection.face);
			}
		}
		return null;
	}
	/*
	public static void transferItems(NodeTransferMode mode, BlockConnection filter, BlockConnection connection, ITransferOverride override) {
		TileEntity filterTile = filter.coords.getTileEntity();
		TileEntity netTile = connection.coords.getTileEntity();
		if (filterTile != null && netTile != null) {
			EnumFacing dirFrom = mode.shouldRemove() ? filter.face : connection.face;
			EnumFacing dirTo = !mode.shouldRemove() ? filter.face : connection.face;
			TileEntity from = mode.shouldRemove() ? filterTile : netTile;
			TileEntity to = !mode.shouldRemove() ? filterTile : netTile;
			ConnectionFilters filters = new ConnectionFilters(override, filter, connection);
			SonarAPI.getItemHelper().transferItems(from, to, dirFrom.getOpposite(), dirTo.getOpposite(), filters);
		}
	}
	*/

	/*
	public static class ConnectionFilters implements IInventoryFilter, ITankFilter, ITransferOverride {

		ITransferOverride override;
		NodeConnection[] connections;

		public ConnectionFilters(ITransferOverride override, NodeConnection... connections) {
			this.override = override;
			this.connections = connections;
		}

		@Override
		public boolean allowed(ItemStack stack) {
			for (NodeConnection connection : connections) {
				if (connection.isFiltered) {
					ITransferFilteredTile tile = (ITransferFilteredTile) connection.source;
					if (!tile.allowed(stack)) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public boolean allowed(FluidStack stack) {
			for (NodeConnection connection : connections) {
				if (connection.isFiltered) {
					ITransferFilteredTile tile = (ITransferFilteredTile) connection.source;
					if (!tile.allowed(stack)) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public void reset() {
			override.reset();
		}

		@Override
		public void add(long added) {
			override.add(added);
		}

		@Override
		public void remove(long removed) {
			override.remove(removed);

		}

		@Override
		public long getMaxRemove() {
			return override.getMaxRemove();
		}

		@Override
		public long getMaxAdd() {
			return override.getMaxAdd();
		}

		@Override
		public ITransferOverride copy() {
			return new ConnectionFilters(override.copy(), connections);
		}

	}
	*/

	public static void onNetworkItemInteraction(IListReader reader, ILogisticsNetwork network, AbstractChangeableList<MonitoredItemStack> abstractChangeableList, EntityPlayer player, ItemStack selected, int button) {
		switch(button){
			case 2:
				if (selected != null) {
					transferNetworkInventoryToPlayer(player, network, IS -> ItemStack.areItemStacksEqual(selected, IS), 64);
				}
				return;
			case 3:
				transferPlayerInventoryToNetwork(player, network, IS -> true, Integer.MAX_VALUE);
				return;
			case 4:
				transferNetworkInventoryToPlayer(player, network, IS -> abstractChangeableList.values.stream().anyMatch(v -> v.getSaveableInfo().getStoredStack().equalStack(IS)), Integer.MAX_VALUE);
				return;
			default:
				if (!player.inventory.getItemStack().isEmpty()) {
					insertItemStack(network, player.inventory.getItemStack(), 64);
				} else if (player.inventory.getItemStack().isEmpty()) {
					if (selected != null) {
						extractItemStack(network, IS -> ItemStack.areItemStacksEqual(selected, IS), 64);
					}
				}
				return;
		}
	}
}
