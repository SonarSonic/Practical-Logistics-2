package sonar.logistics.helpers;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import sonar.core.SonarCore;
import sonar.core.api.SonarAPI;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.ISonarInventoryHandler;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;
import sonar.core.helpers.FluidHelper.ITankFilter;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.core.helpers.InventoryHelper.ITransferOverride;
import sonar.core.helpers.SonarHelper;
import sonar.core.network.PacketInvUpdate;
import sonar.core.network.PacketStackUpdate;
import sonar.core.utils.Pair;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.ItemChangeableList;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.tiles.readers.IWirelessStorageReader;
import sonar.logistics.api.tiles.readers.InventoryReader.SortingType;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.wrappers.ItemWrapper;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.networking.channels.ItemNetworkChannels;

public class ItemHelper extends ItemWrapper {

	public StoredItemStack transferItems(ILogisticsNetwork network, StoredItemStack add, NodeTransferMode mode, ActionType action, IInventoryFilter filter) {
		if (!validStack(add)) {
			return add;
		}
		return NetworkHelper.forEachTileEntity(network, CacheType.ALL, c -> c.canTransferItem(c, add, mode), getTileAction(add, mode, action, filter)) ? add : null;
	}

	private BiPredicate<BlockConnection, TileEntity> getTileAction(StoredItemStack stack, NodeTransferMode mode, ActionType action, IInventoryFilter filter) {
		return (c, t) -> stack.setStackSize(transfer(mode, t, stack, c.face, action)).getStackSize() != 0;
	}

	private StoredItemStack transfer(NodeTransferMode mode, TileEntity tile, StoredItemStack stack, EnumFacing dir, ActionType action) {
		List<ISonarInventoryHandler> handlers = SonarCore.inventoryHandlers;
		for (ISonarInventoryHandler handler : handlers) {
			if (handler.canHandleItems(tile, dir)) {
				StoredItemStack copy = stack.copy().setStackSize(stack);
				return stack = mode.shouldRemove() ? handler.removeStack(copy, tile, dir, action) : handler.addStack(stack, tile, dir, action);
			}
		}
		return null;
	}

	public static boolean validStack(StoredItemStack stack) {
		return stack != null && stack.getStackSize() != 0;
	}

	public StorageSize getTileInventory(List<StoredItemStack> storedStacks, StorageSize storage, List<BlockConnection> connections) {
		for (BlockConnection entry : connections) {
			storage = getTileInventory(storedStacks, storage, entry);
		}
		return storage;
	}

	public StorageSize getTileInventory(List<StoredItemStack> storedStacks, StorageSize storage, BlockConnection entry) {
		TileEntity tile = entry.coords.getTileEntity();
		if (tile == null) {
			return storage;
		}
		boolean specialProvider = false;
		for (ISonarInventoryHandler provider : SonarCore.inventoryHandlers) {
			if (provider.canHandleItems(tile, entry.face)) {
				if (!specialProvider) {
					StorageSize size = provider.getItems(storedStacks, tile, entry.face);
					if (size != StorageSize.EMPTY) {
						specialProvider = true;
						storage.add(size);
					}
				} else {
					continue;
				}
			}
		}
		return storage;
	}

	public StorageSize getEntityInventory(List<StoredItemStack> storedStacks, StorageSize storage, List<Entity> entityList) {
		for (Entity entity : entityList) {
			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				StorageSize size = SonarAPI.getItemHelper().addInventoryToList(storedStacks, player.inventory);
				storage.add(size);
			}
		}
		return storage;

	}

	public long getItemCount(ItemStack stack, ILogisticsNetwork network) {
		if (network.isValid()) {
			ItemNetworkChannels channels = network.getNetworkChannels(ItemNetworkChannels.class);
			channels.updateLargeInventory = true;
			channels.updateAllChannels();
			channels.updateLargeInventory = false;
			ItemChangeableList updateList = new ItemChangeableList();
			for (Entry<NodeConnection, AbstractChangeableList> entry : channels.channels.entrySet()) {
				AbstractChangeableList list = entry.getValue();
				if (!list.getList().isEmpty() && list instanceof ItemChangeableList) {
					for (IMonitoredValue<MonitoredItemStack> coordInfo : ((ItemChangeableList)list).getList()) {
						updateList.add((MonitoredItemStack) coordInfo.getSaveableInfo().copy());
					}
				}
			}
			return updateList.getItemCount(stack);
		}
		return 0;

	}

	public void addItemsFromPlayer(StoredItemStack add, EntityPlayer player, ILogisticsNetwork network, ActionType action, IInventoryFilter filter) {
		IInventory inv = player.inventory;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && stack.getCount() != 0 && add.equalStack(stack)) {
				StoredItemStack toAdd = new StoredItemStack(stack.copy());
				StoredItemStack perform = transferItems(network, toAdd.copy(), NodeTransferMode.ADD, ActionType.PERFORM, filter);
				if (!toAdd.equals(perform)) {
					inv.setInventorySlotContents(i, StoredItemStack.getActualStack(perform));
					inv.markDirty();
				}
			}
		}
	}

	public static StoredItemStack getEntityStack(EntityConnection connection, int slot) {
		if (connection.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) connection.entity;
			IInventory inv = (IInventory) player.inventory;
			if (slot < inv.getSizeInventory()) {
				ItemStack stack = inv.getStackInSlot(slot);
				if (stack == null) {
					return null;
				} else {
					return new StoredItemStack(stack);
				}
			}
		}
		return null;
	}

	public static StoredItemStack getTileStack(BlockConnection connection, int slot) {
		for (ISonarInventoryHandler provider : SonarCore.inventoryHandlers) {
			TileEntity tile = connection.coords.getTileEntity();
			if (tile != null && provider.canHandleItems(tile, connection.face)) {
				return provider.getStack(slot, tile, connection.face);
			}
		}
		return null;
	}

	public StoredItemStack addStackToPlayer(StoredItemStack add, EntityPlayer player, boolean enderChest, ActionType action) {
		if (add == null) {
			return null;
		}
		IInventory inv = null;
		int size = 0;
		if (!enderChest) {
			inv = player.inventory;
			size = player.inventory.mainInventory.size();
		} else {
			inv = player.getInventoryEnderChest();
			size = inv.getSizeInventory();
		}
		if (inv == null || size == 0) {
			return add;
		}
		List<Integer> empty = Lists.newArrayList();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null) {
				if (!(stack.getCount() >= stack.getMaxStackSize()) && add.equalStack(stack) && stack.getCount() < inv.getInventoryStackLimit()) {
					long used = (long) Math.min(add.item.getMaxStackSize(), Math.min(add.stored, inv.getInventoryStackLimit() - stack.getCount()));
					stack.grow((int) used);
					add.stored -= used;
					if (used != 0 && !action.shouldSimulate()) {
						inv.setInventorySlotContents(i, stack);
						if (!enderChest) {
							SonarCore.network.sendTo(new PacketInvUpdate(i, stack), (EntityPlayerMP) player);
						}
					}
					if (add.stored == 0) {
						return null;
					}
				}

			} else {
				empty.add(i);
			}

		}
		for (Integer slot : empty) {
			ItemStack stack = add.item.copy();
			if (inv.isItemValidForSlot(slot, stack)) {
				int used = (int) Math.min(add.stored, inv.getInventoryStackLimit());
				stack.setCount(used);
				add.stored -= used;
				if (!action.shouldSimulate()) {
					inv.setInventorySlotContents(slot, stack);
					if (!enderChest) {
						SonarCore.network.sendTo(new PacketInvUpdate(slot, stack), (EntityPlayerMP) player);
					}
				}
				if (add.stored == 0) {
					return null;
				}
			}
		}
		return add;
	}

	public StoredItemStack removeStackFromPlayer(StoredItemStack remove, EntityPlayer player, boolean enderChest, ActionType action) {
		if (remove == null) {
			return null;
		}
		IInventory inv = null;
		int size = 0;
		inv = !enderChest ? player.inventory : player.getInventoryEnderChest();
		size = !enderChest ? player.inventory.mainInventory.size() : inv.getSizeInventory();
		if (inv == null || size == 0) {
			return remove;
		}
		for (int i = 0; i < size; i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && remove.equalStack(stack)) {
				stack = stack.copy();
				long used = (long) Math.min(remove.stored, Math.min(inv.getInventoryStackLimit(), stack.getCount()));
				stack.shrink((int) used);
				remove.stored -= used;
				if (!action.shouldSimulate()) {
					if (stack.getCount() == 0) {
						stack = ItemStack.EMPTY;
					}
					inv.setInventorySlotContents(i, stack);
				}
				if (remove.stored == 0) {
					return null;
				}
			}
		}
		return remove;
	}

	public StoredItemStack removeToPlayerInventory(StoredItemStack stack, long extractSize, ILogisticsNetwork network, EntityPlayer player, ActionType type) {
		StoredItemStack simulate = SonarAPI.getItemHelper().getStackToAdd(extractSize, stack, transferItems(network, stack.copy().setStackSize(extractSize), NodeTransferMode.REMOVE, type, null));
		if (simulate == null) {
			return null;
		}
		StoredItemStack returned = SonarAPI.getItemHelper().getStackToAdd(stack.stored, simulate, addStackToPlayer(simulate.copy(), player, false, type));
		return returned;
	}

	public StoredItemStack addFromPlayerInventory(StoredItemStack stack, long extractSize, ILogisticsNetwork network, EntityPlayer player, ActionType type) {
		StoredItemStack simulate = SonarAPI.getItemHelper().getStackToAdd(extractSize, stack, removeStackFromPlayer(stack.copy().setStackSize(extractSize), player, false, type));
		if (simulate == null) {
			return null;
		}
		StoredItemStack returned = SonarAPI.getItemHelper().getStackToAdd(stack.stored, simulate, transferItems(network, simulate.copy(), NodeTransferMode.ADD, type, null));
		return returned;

	}

	public StoredItemStack extractItem(ILogisticsNetwork cache, StoredItemStack stack) {
		if (stack != null && stack.stored != 0) {
			StoredItemStack extract = transferItems(cache, stack.copy(), NodeTransferMode.REMOVE, ActionType.PERFORM, null);
			StoredItemStack toAdd = SonarAPI.getItemHelper().getStackToAdd(stack.getStackSize(), stack, extract);
			return toAdd;
		}
		return null;
	}

	public long insertInventoryFromPlayer(EntityPlayer player, ILogisticsNetwork cache, int slotID) {
		ItemStack add = null;
		if (slotID == -1) {
			add = player.inventory.getItemStack();
		} else
			add = player.inventory.getStackInSlot(slotID);
		if (add == null) {
			return 0;
		}
		StoredItemStack stack = new StoredItemStack(add).setStackSize(0);
		IInventory inv = player.inventory;
		List<Integer> slots = Lists.newArrayList();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (stack.equalStack(item)) {
				stack.add(item);
				slots.add(i);
			}
		}
		StoredItemStack remainder = transferItems(cache, stack.copy(), NodeTransferMode.ADD, ActionType.PERFORM, null);
		StoredItemStack toAdd = SonarAPI.getItemHelper().getStackToAdd(stack.getStackSize(), stack, remainder);
		PL2API.getItemHelper().removeStackFromPlayer(toAdd.copy(), player, false, ActionType.PERFORM);
		return toAdd.getStackSize();
	}

	public long insertItemFromPlayer(EntityPlayer player, ILogisticsNetwork cache, int slot) {
		ItemStack add = player.inventory.getStackInSlot(slot);
		if (add == null)
			return 0;
		int original = add.getCount();
		StoredItemStack stack = transferItems(cache, new StoredItemStack(add), NodeTransferMode.ADD, ActionType.PERFORM, null);
		ItemStack returned = StoredItemStack.getActualStack(stack);
		if (!ItemStack.areItemStacksEqual(returned, player.inventory.getStackInSlot(slot))) {
			player.inventory.setInventorySlotContents(slot, returned);
			return (returned == null ? original : original - returned.getCount());
		} else {
			FontHelper.sendMessage(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "The item cannot be inserted", player.getEntityWorld(), player);
			return 0;
		}
	}

	public boolean dumpInventoryFromPlayer(EntityPlayer player, ILogisticsNetwork cache) {
		boolean change = false;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack add = player.inventory.getStackInSlot(i);
			if (add == null || add.getItem() instanceof IWirelessStorageReader)
				continue;
			StoredItemStack stack = transferItems(cache, new StoredItemStack(add), NodeTransferMode.ADD, ActionType.PERFORM, null);
			if (stack == null || stack.stored == 0) {
				add = null;
			} else {
				add.setCount((int) stack.stored);
			}
			if (!ItemStack.areItemStacksEqual(add, player.inventory.getStackInSlot(i))) {
				change = true;
				player.inventory.setInventorySlotContents(i, add);
			}
		}
		return change;
	}

	public void dumpNetworkToPlayer(AbstractChangeableList<MonitoredItemStack> items, EntityPlayer player, ILogisticsNetwork cache) {
		for (IMonitoredValue<MonitoredItemStack> value : items.getList()) {
			MonitoredItemStack stack = value.getSaveableInfo();
			StoredItemStack returned = removeToPlayerInventory(stack.getStoredStack(), stack.getStored(), cache, player, ActionType.SIMULATE);
			if (returned != null) {
				removeToPlayerInventory(stack.getStoredStack(), returned.stored, cache, player, ActionType.PERFORM);
			}
		}
	}

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

	// TODO clean up this mess
	public static void onNetworkItemInteraction(IListReader reader, ILogisticsNetwork network, AbstractChangeableList<MonitoredItemStack> abstractChangeableList, EntityPlayer player, ItemStack selected, int button) {
		if (button == 3) {
			PL2API.getItemHelper().dumpInventoryFromPlayer(player, network);
		} else if (button == 4) {
			PL2API.getItemHelper().dumpNetworkToPlayer(abstractChangeableList, player, network);
		} else if (button == 2) {
			if (selected == null) {
				return;
			}
			PL2API.getItemHelper().removeToPlayerInventory(new StoredItemStack(selected), (long) 64, network, player, ActionType.PERFORM);
		} else if (player.inventory.getItemStack() != null) {
			StoredItemStack add = new StoredItemStack(player.inventory.getItemStack().copy());
			int stackSize = Math.min(button == 1 ? 1 : 64, add.getValidStackSize());
			StoredItemStack stack = PL2API.getItemHelper().transferItems(network, add.copy().setStackSize(stackSize), NodeTransferMode.ADD, ActionType.PERFORM, null);
			StoredItemStack remove = SonarAPI.getItemHelper().getStackToAdd(stackSize, add, stack);
			ItemStack actualStack = add.copy().setStackSize(add.stored - SonarAPI.getItemHelper().getStackToAdd(stackSize, add, stack).stored).getActualStack();
			if (actualStack == null || (actualStack.getCount() != add.stored && !(actualStack.getCount() <= 0)) && !ItemStack.areItemStacksEqual(StoredItemStack.getActualStack(stack), player.inventory.getItemStack())) {
				player.inventory.setItemStack(actualStack);
				SonarCore.network.sendTo(new PacketStackUpdate(actualStack), (EntityPlayerMP) player);
			}
		} else if (player.inventory.getItemStack() == null) {
			if (selected == null) {
				return;
			}
			ItemStack stack = selected;
			StoredItemStack toAdd = new StoredItemStack(stack.copy()).setStackSize(Math.min(stack.getMaxStackSize(), 64));
			StoredItemStack removed = PL2API.getItemHelper().transferItems(network, toAdd.copy(), NodeTransferMode.REMOVE, ActionType.SIMULATE, null);
			StoredItemStack simulate = SonarAPI.getItemHelper().getStackToAdd(toAdd.stored, toAdd, removed);
			if (simulate != null && simulate.stored != 0) {
				if (button == 1 && simulate.stored != 1) {
					simulate.setStackSize((long) Math.ceil(simulate.getStackSize() / 2));
				}
				StoredItemStack storedStack = SonarAPI.getItemHelper().getStackToAdd(simulate.stored, simulate, PL2API.getItemHelper().transferItems(network, simulate.copy(), NodeTransferMode.REMOVE, ActionType.PERFORM, null));
				if (storedStack != null && storedStack.stored != 0) {
					player.inventory.setItemStack(storedStack.getFullStack());
					SonarCore.network.sendTo(new PacketStackUpdate(storedStack.getFullStack()), (EntityPlayerMP) player);
				}
			}

		}
		ItemNetworkChannels channels = network.getNetworkChannels(ItemNetworkChannels.class);
		if (channels != null) // TODO shouldn't have to ever do this.
			channels.sendLocalRapidUpdate(reader, player);
	}

	public static void sortItemList(AbstractChangeableList<MonitoredItemStack> updateInfo, final SortingDirection dir, SortingType type) {
		updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredItemStack>>() {
			public int compare(IMonitoredValue<MonitoredItemStack> str1,IMonitoredValue<MonitoredItemStack> str2) {
				StoredItemStack item1 = str1.getSaveableInfo().getStoredStack(), item2 = str2.getSaveableInfo().getStoredStack();
				return SonarHelper.compareStringsWithDirection(item1.getItemStack().getDisplayName(), item2.getItemStack().getDisplayName(), dir);
			}
		});

		switch (type) {
		case STORED:
			updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredItemStack>>() {
				public int compare(IMonitoredValue<MonitoredItemStack> str1, IMonitoredValue<MonitoredItemStack> str2) {
					StoredItemStack item1 = str1.getSaveableInfo().getStoredStack(), item2 = str2.getSaveableInfo().getStoredStack();
					return SonarHelper.compareWithDirection(item1.stored, item2.stored, dir);
				}
			});
			break;
		case MODID:
			updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredItemStack>>() {
				public int compare(IMonitoredValue<MonitoredItemStack> str1, IMonitoredValue<MonitoredItemStack> str2) {
					StoredItemStack item1 = str1.getSaveableInfo().getStoredStack(), item2 = str2.getSaveableInfo().getStoredStack();
					String modid1 = item1.getItemStack().getItem().getRegistryName().getResourceDomain();
					String modid2 = item2.getItemStack().getItem().getRegistryName().getResourceDomain();
					return SonarHelper.compareStringsWithDirection(modid1, modid2, dir);
				}
			});
		default:
			break;
		}
	}
}
