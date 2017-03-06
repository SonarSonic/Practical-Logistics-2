package sonar.logistics.helpers;

import java.util.ArrayList;
import java.util.Comparator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import sonar.core.SonarCore;
import sonar.core.api.SonarAPI;
import sonar.core.api.fluids.ISonarFluidHandler;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;
import sonar.core.helpers.SonarHelper;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.readers.FluidReader.SortingType;
import sonar.logistics.api.wrappers.FluidWrapper;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;

public class FluidHelper extends FluidWrapper {

	public StoredFluidStack addFluids(StoredFluidStack add, INetworkCache network, ActionType action) {
		if (add.stored == 0) {
			return add;
		}
		ArrayList<BlockConnection> connections = network.getExternalBlocks(true);
		connections: for (BlockConnection entry : connections) {
			if (!entry.canTransferFluid(entry.coords, add, NodeTransferMode.ADD)) {
				continue;
			}
			TileEntity tile = entry.coords.getTileEntity();
			if (tile != null) {
				for (ISonarFluidHandler provider : SonarCore.fluidHandlers) {
					if (provider.canHandleFluids(tile, entry.face)) {
						add = provider.addStack(add, tile, entry.face, action);
						if (add == null || add.stored == 0) {
							return null;
						}
						continue connections;
					}
				}
			}
		}
		return add;
	}

	public StoredFluidStack removeFluids(StoredFluidStack remove, INetworkCache network, ActionType action) {
		if (remove.stored == 0) {
			return remove;
		}
		ArrayList<BlockConnection> connections = network.getExternalBlocks(true);
		for (BlockConnection entry : connections) {
			remove = removeFluids(remove, entry, action);
			if (remove == null) {
				return null;
			}
		}
		return remove;
	}

	public StoredFluidStack removeFluids(StoredFluidStack remove, BlockConnection connection, ActionType type) {
		if (!connection.canTransferFluid(connection.coords, remove, NodeTransferMode.REMOVE)) {
			return remove;
		}
		TileEntity tile = connection.coords.getTileEntity();
		if (tile != null) {
			for (ISonarFluidHandler provider : SonarCore.fluidHandlers) {
				if (provider.canHandleFluids(tile, connection.face)) {
					remove = provider.removeStack(remove, tile, connection.face, type);
					if (remove == null) {
						return null;
					}
					break;
				}
			}
		}
		return remove;
	}

	public StoredFluidStack addFluids(StoredFluidStack remove, BlockConnection connection, ActionType type) {
		if (!connection.canTransferFluid(connection.coords, remove, NodeTransferMode.ADD)) {
			return remove;
		}
		TileEntity tile = connection.coords.getTileEntity();
		if (tile != null) {
			for (ISonarFluidHandler provider : SonarCore.fluidHandlers) {
				if (provider.canHandleFluids(tile, connection.face)) {
					remove = provider.addStack(remove, tile, connection.face, type);
					if (remove == null) {
						return null;
					}
					break;
				}
			}
		}
		return remove;
	}
	
	public int fillCapabilityStack(ItemStack container, StoredFluidStack fill, INetworkCache network, ActionType action) {
		if (container.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
			return container.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).fill(fill.getFullStack(), !action.shouldSimulate());
		}
		return 0;
	}

	/** if simulating your expected to pass copies of both the container and stack to fill with */
	public FluidStack drainCapabilityStack(ItemStack container, int toDrain, INetworkCache network, ActionType action) {
		if (container.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
			IFluidHandler handler = container.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);

			FluidStack stack = handler.getTankProperties()[0].getContents();
			if (stack != null && stack.amount >= 0) {
				StoredFluidStack add = new StoredFluidStack(stack, Math.min(toDrain, stack.amount));
				StoredFluidStack added = SonarAPI.getFluidHelper().getStackToAdd(toDrain, add, addFluids(add.copy(), network, ActionType.SIMULATE));
				if (added == null || added.stored >= 0) {
					return handler.drain((int) added.stored, !action.shouldSimulate());
				}
			}
			return null;
		}
		return null;
	}

	public void fillHeldItem(EntityPlayer player, INetworkCache cache, StoredFluidStack toFill) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (heldItem == null || toFill == null) {
			return;
		}
		heldItem = heldItem.copy();
		heldItem.stackSize = 1;

		StoredFluidStack remaining = removeFluids(toFill.copy(), cache, ActionType.SIMULATE);
		StoredFluidStack removed = SonarAPI.getFluidHelper().getStackToAdd(toFill.getStackSize(), toFill, remaining);
		if (removed.stored <= 0) {
			return;
		}
		int filled = fillCapabilityStack(heldItem.copy(), removed, cache, ActionType.SIMULATE);
		if (filled != 0) {
			ItemStack toAdd = heldItem.copy();
			removed = SonarAPI.getFluidHelper().getStackToAdd(toFill.getStackSize(), toFill, removeFluids(new StoredFluidStack(toFill.getFullStack(), filled, toFill.capacity), cache, ActionType.PERFORM));
			int fill = fillCapabilityStack(toAdd, removed, cache, ActionType.PERFORM);
			if (player.getHeldItemMainhand().stackSize != 1) {
				player.inventory.decrStackSize(player.inventory.currentItem, 1);
				LogisticsAPI.getItemHelper().addStackToPlayer(new StoredItemStack(toAdd), player, false, ActionType.PERFORM);
			} else {
				player.inventory.setInventorySlotContents(player.inventory.currentItem, toAdd);
			}
		}
	}

	public void drainHeldItem(EntityPlayer player, INetworkCache cache, int toDrain) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (heldItem == null || toDrain <= 0) {
			return;
		}
		FluidStack drained = drainCapabilityStack(heldItem.copy(), toDrain, cache, ActionType.SIMULATE);
		if (drained != null && drained.amount > 0) {
			ItemStack toAdd = heldItem.copy();
			addFluids(new StoredFluidStack(drainCapabilityStack(toAdd, toDrain, cache, ActionType.PERFORM)), cache, ActionType.PERFORM);
			if (heldItem.stackSize != 1) {
				player.inventory.decrStackSize(player.inventory.currentItem, 1);
				LogisticsAPI.getItemHelper().addStackToPlayer(new StoredItemStack(toAdd), player, false, ActionType.PERFORM);
			} else {
				player.inventory.setInventorySlotContents(player.inventory.currentItem, toAdd);
			}
		}
	}

	public static void sortFluidList(ArrayList<MonitoredFluidStack> current, final SortingDirection dir, SortingType type) {
		current.sort(new Comparator<MonitoredFluidStack>() {
			public int compare(MonitoredFluidStack str1, MonitoredFluidStack str2) {
				StoredFluidStack flu1 = str1.fluidStack.getObject(), flu2 = str2.fluidStack.getObject();
				int res = String.CASE_INSENSITIVE_ORDER.compare(flu1.getFullStack().getLocalizedName(), flu2.getFullStack().getLocalizedName());
				if (res == 0) {
					res = flu1.getFullStack().getLocalizedName().compareTo(flu2.getFullStack().getLocalizedName());
				}
				return dir == SortingDirection.DOWN ? res : -res;
			}
		});

		current.sort(new Comparator<MonitoredFluidStack>() {
			public int compare(MonitoredFluidStack str1, MonitoredFluidStack str2) {
				StoredFluidStack flu1 = str1.fluidStack.getObject(), flu2 = str2.fluidStack.getObject();
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
