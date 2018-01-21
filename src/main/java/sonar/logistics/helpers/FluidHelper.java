package sonar.logistics.helpers;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import sonar.core.SonarCore;
import sonar.core.api.SonarAPI;
import sonar.core.api.fluids.ISonarFluidHandler;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;
import sonar.core.helpers.FluidHelper.ITankFilter;
import sonar.core.helpers.SonarHelper;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.tiles.readers.FluidReader.SortingType;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.wrappers.FluidWrapper;
import sonar.logistics.helpers.ItemHelper.ConnectionFilters;
import sonar.logistics.info.types.MonitoredFluidStack;

public class FluidHelper extends FluidWrapper {

	public StoredFluidStack transferFluids(ILogisticsNetwork network, StoredFluidStack add, NodeTransferMode mode, ActionType action, ITankFilter filter) {
		if (!validStack(add)) {
			return add;
		}
		return NetworkHelper.forEachTileEntity(network, CacheType.ALL, c -> c.canTransferFluid(c, add, mode), getTileAction(add, mode, action, filter)) ? add : null;
	}

	private BiPredicate<BlockConnection, TileEntity> getTileAction(StoredFluidStack stack, NodeTransferMode mode, ActionType action, ITankFilter filter) {
		return (c, t) -> stack.setStackSize(transfer(mode, t, stack, c.face, action)).getStackSize() != 0;
	}

	private StoredFluidStack transfer(NodeTransferMode mode, TileEntity tile, StoredFluidStack stack, EnumFacing dir, ActionType action) {
		List<ISonarFluidHandler> handlers = SonarCore.fluidHandlers;
		for (ISonarFluidHandler handler : handlers) {
			if (handler.canHandleFluids(tile, dir)) {
				StoredFluidStack copy = stack.copy().setStackSize(stack);
				return stack = mode.shouldRemove() ? handler.removeStack(copy, tile, dir, action) : handler.addStack(stack, tile, dir, action);
			}
		}
		return null;
	}

	public static boolean validStack(StoredFluidStack stack) {
		return stack != null && stack.getStackSize() != 0;
	}

	public int fillCapabilityStack(ItemStack container, StoredFluidStack fill, ILogisticsNetwork network, ActionType action) {
		if (container.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
			return container.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).fill(fill.getFullStack(), !action.shouldSimulate());
		}
		return 0;
	}

	/** if simulating your expected to pass copies of both the container and stack to fill with */
	public FluidStack drainCapabilityStack(ItemStack container, int toDrain, ILogisticsNetwork network, ActionType action) {
		if (container.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
			IFluidHandler handler = container.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);

			FluidStack stack = handler.getTankProperties()[0].getContents();
			if (stack != null && stack.amount >= 0) {
				StoredFluidStack add = new StoredFluidStack(stack, Math.min(toDrain, stack.amount));
				StoredFluidStack added = SonarAPI.getFluidHelper().getStackToAdd(toDrain, add, transferFluids(network, add.copy(), null, ActionType.SIMULATE, null));
				if (added == null || added.stored >= 0) {
					return handler.drain((int) added.stored, !action.shouldSimulate());
				}
			}
			return null;
		}
		return null;
	}

	public void fillHeldItem(EntityPlayer player, ILogisticsNetwork cache, StoredFluidStack toFill) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (heldItem == null || toFill == null) {
			return;
		}
		heldItem = heldItem.copy();
		heldItem.shrink(1);

		StoredFluidStack remaining = transferFluids(cache, toFill.copy(), NodeTransferMode.REMOVE, ActionType.SIMULATE, null);
		StoredFluidStack removed = SonarAPI.getFluidHelper().getStackToAdd(toFill.getStackSize(), toFill, remaining);
		if (removed.stored <= 0) {
			return;
		}
		int filled = fillCapabilityStack(heldItem.copy(), removed, cache, ActionType.SIMULATE);
		if (filled != 0) {
			ItemStack toAdd = heldItem.copy();
			removed = SonarAPI.getFluidHelper().getStackToAdd(toFill.getStackSize(), toFill, transferFluids(cache, new StoredFluidStack(toFill.getFullStack(), filled, toFill.capacity), NodeTransferMode.REMOVE, ActionType.PERFORM, null));
			int fill = fillCapabilityStack(toAdd, removed, cache, ActionType.PERFORM);
			if (player.getHeldItemMainhand().getCount() != 1) {
				player.inventory.decrStackSize(player.inventory.currentItem, 1);
				PL2API.getItemHelper().addStackToPlayer(new StoredItemStack(toAdd), player, false, ActionType.PERFORM);
			} else {
				player.inventory.setInventorySlotContents(player.inventory.currentItem, toAdd);
			}
		}
	}

	public void drainHeldItem(EntityPlayer player, ILogisticsNetwork cache, int toDrain) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (heldItem == null || toDrain <= 0) {
			return;
		}
		FluidStack drained = drainCapabilityStack(heldItem.copy(), toDrain, cache, ActionType.SIMULATE);
		if (drained != null && drained.amount > 0) {
			ItemStack toAdd = heldItem.copy();
			transferFluids(cache, new StoredFluidStack(drainCapabilityStack(toAdd, toDrain, cache, ActionType.PERFORM)), null, ActionType.PERFORM, null);
			if (heldItem.getCount() != 1) {
				player.inventory.decrStackSize(player.inventory.currentItem, 1);
				PL2API.getItemHelper().addStackToPlayer(new StoredItemStack(toAdd), player, false, ActionType.PERFORM);
			} else {
				player.inventory.setInventorySlotContents(player.inventory.currentItem, toAdd);
			}
		}
	}

	public static void transferFluids(NodeTransferMode mode, BlockConnection filter, BlockConnection connection) {
		TileEntity filterTile = filter.coords.getTileEntity();
		TileEntity netTile = connection.coords.getTileEntity();
		if (filterTile != null && netTile != null) {
			EnumFacing dirFrom = mode.shouldRemove() ? filter.face : connection.face;
			EnumFacing dirTo = !mode.shouldRemove() ? filter.face : connection.face;
			TileEntity from = mode.shouldRemove() ? filterTile : netTile;
			TileEntity to = !mode.shouldRemove() ? filterTile : netTile;
			ConnectionFilters filters = new ConnectionFilters(null, filter, connection);

			SonarAPI.getFluidHelper().transferFluids(from, to, dirFrom.getOpposite(), dirTo.getOpposite(), filters);
		}
	}

	public static void sortFluidList(AbstractChangeableList<MonitoredFluidStack> updateInfo, final SortingDirection dir, SortingType type) {
		updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredFluidStack>>() {
			public int compare(IMonitoredValue<MonitoredFluidStack> str1, IMonitoredValue<MonitoredFluidStack> str2) {
				StoredFluidStack flu1 = str1.getSaveableInfo().getStoredStack(), flu2 = str2.getSaveableInfo().getStoredStack();
				int res = String.CASE_INSENSITIVE_ORDER.compare(flu1.getFullStack().getLocalizedName(), flu2.getFullStack().getLocalizedName());
				if (res == 0) {
					res = flu1.getFullStack().getLocalizedName().compareTo(flu2.getFullStack().getLocalizedName());
				}
				return dir == SortingDirection.DOWN ? res : -res;
			}
		});

		updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredFluidStack>>() {
			public int compare(IMonitoredValue<MonitoredFluidStack> str1, IMonitoredValue<MonitoredFluidStack> str2) {
				StoredFluidStack flu1 = str1.getSaveableInfo().getStoredStack(), flu2 = str2.getSaveableInfo().getStoredStack();
				switch (type) {
				case MODID:
					return SonarHelper.compareStringsWithDirection(flu1.getFullStack().getFluid().getBlock().getRegistryName().getResourceDomain(), flu2.getFullStack().getFluid().getBlock().getRegistryName().getResourceDomain(), dir);
				case NAME:
					break;
				case STORED:
					return SonarHelper.compareWithDirection(flu1.stored, flu2.stored, dir);
				case TEMPERATURE:
					return SonarHelper.compareWithDirection(flu1.getFullStack().getFluid().getTemperature(), flu2.getFullStack().getFluid().getTemperature(), dir);
				}
				return 0;
			}
		});
	}
}
