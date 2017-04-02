package sonar.logistics.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import sonar.core.handlers.inventories.IInventoryHandler;
import sonar.core.helpers.FluidHelper.ITankFilter;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.core.helpers.SonarHelper;
import sonar.core.network.PacketInvUpdate;
import sonar.core.network.PacketStackUpdate;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.readers.InventoryReader.SortingType;
import sonar.logistics.api.wrappers.ItemWrapper;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;

public class ItemHelper extends ItemWrapper {

	public StorageSize getTileInventory(List<StoredItemStack> storedStacks, StorageSize storage, ArrayList<BlockConnection> connections) {
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

	public StoredItemStack addItems(StoredItemStack add, INetworkCache network, ActionType action) {
		ArrayList<NodeConnection> connections = network.getConnectedChannels(true);
		for (NodeConnection entry : connections) {
			if (!entry.canTransferItem(entry, add, NodeTransferMode.ADD)) {
				continue;
			}
			if (entry instanceof BlockConnection) {
				BlockConnection connection = (BlockConnection) entry;
				TileEntity tile = connection.coords.getTileEntity();
				if (tile == null) {
					continue;
				}
				for (ISonarInventoryHandler provider : SonarCore.inventoryHandlers) {
					if (provider.canHandleItems(tile, connection.face)) {
						add = provider.addStack(add, tile, connection.face, action);
						if (add == null) {
							return null;
						}
						break; // make sure to only use one InventoryHandler!!
					}
				}
			}
		}
		return add;
	}

	public void addItemsFromPlayer(StoredItemStack add, EntityPlayer player, INetworkCache network, ActionType action) {
		IInventory inv = player.inventory;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && stack.stackSize != 0 && add.equalStack(stack)) {
				StoredItemStack toAdd = new StoredItemStack(stack.copy());
				StoredItemStack perform = LogisticsAPI.getItemHelper().addItems(toAdd.copy(), network, ActionType.PERFORM);
				if (!toAdd.equals(perform)) {
					inv.setInventorySlotContents(i, StoredItemStack.getActualStack(perform));
					inv.markDirty();
				}
			}
		}
	}

	public StoredItemStack removeItems(StoredItemStack remove, INetworkCache network, ActionType action) {
		ArrayList<NodeConnection> connections = network.getConnectedChannels(true);
		for (NodeConnection entry : connections) {
			if (!entry.canTransferItem(entry, remove, NodeTransferMode.REMOVE)) {
				continue;
			}
			if (entry instanceof BlockConnection) {
				BlockConnection connection = (BlockConnection) entry;
				TileEntity tile = connection.coords.getTileEntity();
				if (tile == null) {
					continue;
				}
				for (ISonarInventoryHandler provider : SonarCore.inventoryHandlers) {
					if (provider instanceof IInventoryHandler) {
						continue;
					}
					if (provider.canHandleItems(tile, connection.face)) {
						remove = provider.removeStack(remove, tile, connection.face, action);
						if (remove == null) {
							return null;
						}
						break; // make sure to only use one InventoryHandler!!
					}
				}
			}
		}
		return remove;
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
			size = player.inventory.mainInventory.length;
		} else {
			inv = player.getInventoryEnderChest();
			size = inv.getSizeInventory();
		}
		if (inv == null || size == 0) {
			return add;
		}
		List<Integer> empty = new ArrayList();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null) {
				if (!(stack.stackSize >= stack.getMaxStackSize()) && add.equalStack(stack) && stack.stackSize < inv.getInventoryStackLimit()) {
					long used = (long) Math.min(add.item.getMaxStackSize(), Math.min(add.stored, inv.getInventoryStackLimit() - stack.stackSize));
					stack.stackSize += used;
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
				stack.stackSize = used;
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
		size = !enderChest ? player.inventory.mainInventory.length : inv.getSizeInventory();
		if (inv == null || size == 0) {
			return remove;
		}
		for (int i = 0; i < size; i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && remove.equalStack(stack)) {
				stack = stack.copy();
				long used = (long) Math.min(remove.stored, Math.min(inv.getInventoryStackLimit(), stack.stackSize));
				stack.stackSize -= used;
				remove.stored -= used;
				if (!action.shouldSimulate()) {
					if (stack.stackSize == 0) {
						stack = null;
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

	public StoredItemStack removeToPlayerInventory(StoredItemStack stack, long extractSize, INetworkCache network, EntityPlayer player, ActionType type) {
		StoredItemStack simulate = SonarAPI.getItemHelper().getStackToAdd(extractSize, stack, removeItems(stack.copy().setStackSize(extractSize), network, type));
		if (simulate == null) {
			return null;
		}
		StoredItemStack returned = SonarAPI.getItemHelper().getStackToAdd(stack.stored, simulate, addStackToPlayer(simulate.copy(), player, false, type));
		return returned;
	}

	public StoredItemStack addFromPlayerInventory(StoredItemStack stack, long extractSize, INetworkCache network, EntityPlayer player, ActionType type) {
		StoredItemStack simulate = SonarAPI.getItemHelper().getStackToAdd(extractSize, stack, removeStackFromPlayer(stack.copy().setStackSize(extractSize), player, false, type));
		if (simulate == null) {
			return null;
		}
		StoredItemStack returned = SonarAPI.getItemHelper().getStackToAdd(stack.stored, simulate, addItems(simulate.copy(), network, type));
		return returned;

	}

	public StoredItemStack extractItem(INetworkCache cache, StoredItemStack stack) {
		if (stack != null && stack.stored != 0) {
			StoredItemStack extract = LogisticsAPI.getItemHelper().removeItems(stack.copy(), cache, ActionType.PERFORM);
			StoredItemStack toAdd = SonarAPI.getItemHelper().getStackToAdd(stack.getStackSize(), stack, extract);
			return toAdd;
		}
		return null;
	}

	public void insertInventoryFromPlayer(EntityPlayer player, INetworkCache cache, int slotID) {
		ItemStack add = null;
		if (slotID == -1) {
			add = player.inventory.getItemStack();
		} else
			add = player.inventory.getStackInSlot(slotID);
		if (add == null) {
			return;
		}
		StoredItemStack stack = new StoredItemStack(add).setStackSize(0);
		IInventory inv = player.inventory;
		ArrayList<Integer> slots = new ArrayList();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (stack.equalStack(item)) {
				stack.add(item);
				slots.add(i);
			}
		}
		StoredItemStack remainder = LogisticsAPI.getItemHelper().addItems(stack.copy(), cache, ActionType.PERFORM);
		StoredItemStack toAdd = SonarAPI.getItemHelper().getStackToAdd(stack.getStackSize(), stack, remainder);
		LogisticsAPI.getItemHelper().removeStackFromPlayer(toAdd, player, false, ActionType.PERFORM);
	}

	public void insertItemFromPlayer(EntityPlayer player, INetworkCache cache, int slot) {
		ItemStack add = player.inventory.getStackInSlot(slot);
		if (add == null)
			return;
		StoredItemStack stack = LogisticsAPI.getItemHelper().addItems(new StoredItemStack(add), cache, ActionType.PERFORM);
		if (stack == null || stack.stored == 0) {
			add = null;
		} else {
			add.stackSize = (int) stack.stored;
		}
		if (!ItemStack.areItemStacksEqual(add, player.inventory.getStackInSlot(slot))) {
			player.inventory.setInventorySlotContents(slot, add);
		} else {
			FontHelper.sendMessage(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "The item cannot be inserted", player.getEntityWorld(), player);
		}
	}

	public void dumpInventoryFromPlayer(EntityPlayer player, INetworkCache cache) {
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack add = player.inventory.getStackInSlot(i);
			if (add == null)
				continue;
			StoredItemStack stack = LogisticsAPI.getItemHelper().addItems(new StoredItemStack(add), cache, ActionType.PERFORM);
			if (stack == null || stack.stored == 0) {
				add = null;
			} else {
				add.stackSize = (int) stack.stored;
			}
			if (!ItemStack.areItemStacksEqual(add, player.inventory.getStackInSlot(i))) {
				player.inventory.setInventorySlotContents(i, add);
			}
		}
	}

	public void dumpNetworkToPlayer(MonitoredList<MonitoredItemStack> items, EntityPlayer player, INetworkCache cache) {
		for (MonitoredItemStack stack : items) {
			StoredItemStack returned = removeToPlayerInventory(stack.getStoredStack(), stack.getStored(), cache, player, ActionType.SIMULATE);
			if (returned != null) {
				removeToPlayerInventory(stack.getStoredStack(), returned.stored, cache, player, ActionType.PERFORM);
			}
		}
	}

	public static void transferItems(NodeTransferMode mode, BlockConnection filter, BlockConnection connection) {
		TileEntity filterTile = filter.coords.getTileEntity();
		TileEntity netTile = connection.coords.getTileEntity();
		if (filterTile != null && netTile != null) {
			EnumFacing dirFrom = mode.shouldRemove() ? filter.face : connection.face;
			EnumFacing dirTo = !mode.shouldRemove() ? filter.face : connection.face;
			TileEntity from = mode.shouldRemove() ? filterTile : netTile;
			TileEntity to = !mode.shouldRemove() ? filterTile : netTile;
			ConnectionFilters filters = new ConnectionFilters(filter, connection);

			SonarAPI.getItemHelper().transferItems(from, to, dirFrom.getOpposite(), dirTo.getOpposite(), filters);
		}
	}

	public static class ConnectionFilters implements IInventoryFilter, ITankFilter {

		NodeConnection[] connections;

		public ConnectionFilters(NodeConnection... connections) {
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

	}

	public static void onNetworkItemInteraction(INetworkCache network, MonitoredList<MonitoredItemStack> items, EntityPlayer player, ItemStack selected, int button) {
		if (button == 3) {
			LogisticsAPI.getItemHelper().dumpInventoryFromPlayer(player, network);
		} else if (button == 4) {
			LogisticsAPI.getItemHelper().dumpNetworkToPlayer(items, player, network);
		} else if (button == 2) {
			if (selected == null) {
				return;
			}
			LogisticsAPI.getItemHelper().removeToPlayerInventory(new StoredItemStack(selected), (long) 64, network, player, ActionType.PERFORM);
		} else if (player.inventory.getItemStack() != null) {
			StoredItemStack add = new StoredItemStack(player.inventory.getItemStack().copy());
			int stackSize = Math.min(button == 1 ? 1 : 64, add.getValidStackSize());
			StoredItemStack stack = LogisticsAPI.getItemHelper().addItems(add.copy().setStackSize(stackSize), network, ActionType.PERFORM);
			StoredItemStack remove = SonarAPI.getItemHelper().getStackToAdd(stackSize, add, stack);
			ItemStack actualStack = add.copy().setStackSize(add.stored - SonarAPI.getItemHelper().getStackToAdd(stackSize, add, stack).stored).getActualStack();
			if (actualStack == null || (actualStack.stackSize != add.stored && !(actualStack.stackSize <= 0)) && !ItemStack.areItemStacksEqual(StoredItemStack.getActualStack(stack), player.inventory.getItemStack())) {
				player.inventory.setItemStack(actualStack);
				SonarCore.network.sendTo(new PacketStackUpdate(actualStack), (EntityPlayerMP) player);
			}
		} else if (player.inventory.getItemStack() == null) {
			if (selected == null) {
				return;
			}
			ItemStack stack = selected;
			StoredItemStack toAdd = new StoredItemStack(stack.copy()).setStackSize(Math.min(stack.getMaxStackSize(), 64));
			StoredItemStack removed = LogisticsAPI.getItemHelper().removeItems(toAdd.copy(), network, ActionType.SIMULATE);
			StoredItemStack simulate = SonarAPI.getItemHelper().getStackToAdd(toAdd.stored, toAdd, removed);
			if (simulate != null && simulate.stored != 0) {
				if (button == 1 && simulate.stored != 1) {
					simulate.setStackSize((long) Math.ceil(simulate.getStackSize() / 2));
				}
				StoredItemStack storedStack = SonarAPI.getItemHelper().getStackToAdd(simulate.stored, simulate, LogisticsAPI.getItemHelper().removeItems(simulate.copy(), network, ActionType.PERFORM));
				if (storedStack != null && storedStack.stored != 0) {
					player.inventory.setItemStack(storedStack.getFullStack());
					SonarCore.network.sendTo(new PacketStackUpdate(storedStack.getFullStack()), (EntityPlayerMP) player);
				}
			}

		}
	}

	public static void sortItemList(ArrayList<MonitoredItemStack> info, final SortingDirection dir, SortingType type) {
		info.sort(new Comparator<MonitoredItemStack>() {
			public int compare(MonitoredItemStack str1, MonitoredItemStack str2) {
				StoredItemStack item1 = str1.getStoredStack(), item2 = str2.getStoredStack();
				return SonarHelper.compareStringsWithDirection(item1.getItemStack().getDisplayName(), item2.getItemStack().getDisplayName(), dir);
			}
		});

		switch (type) {
		case STORED:
			info.sort(new Comparator<MonitoredItemStack>() {
				public int compare(MonitoredItemStack str1, MonitoredItemStack str2) {
					StoredItemStack item1 = str1.getStoredStack(), item2 = str2.getStoredStack();
					return SonarHelper.compareWithDirection(item1.stored, item2.stored, dir);
				}
			});
			break;
		case MODID:
			info.sort(new Comparator<MonitoredItemStack>() {
				public int compare(MonitoredItemStack str1, MonitoredItemStack str2) {
					StoredItemStack item1 = str1.getStoredStack(), item2 = str2.getStoredStack();
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
