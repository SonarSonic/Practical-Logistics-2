package sonar.logistics.api.wrappers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.utils.ActionType;
import sonar.core.helpers.FluidHelper.ITankFilter;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;

public class FluidWrapper {

	/** used for adding Fluids to the network
	 * @param network the {@link INetworkCache} to add to
	 * @param add {@link StoredFluidStack} to add
	 * @param mode TODO
	 * @param action should this action be simulated
	 * @return remaining {@link StoredFluidStack} (what wasn't added), can be null */
	public StoredFluidStack transferFluids(ILogisticsNetwork network, StoredFluidStack add, NodeTransferMode mode, ActionType action, ITankFilter filter) {
		return add;
	}

	/** used for filling ItemStacks with the network
	 * @param container the {@link ItemStack} to try and fill
	 * @param fill the {@link StoredFluidStack} type to fill with
	 * @param network the {@link INetworkCache} to fill from
	 * @param action should this action be simulated
	 * @return the new ItemStack */
	public ItemStack fillFluidItemStack(ItemStack container, StoredFluidStack fill, ILogisticsNetwork network, ActionType action) {
		return container;
	}

	/** used for draining ItemStacks with the network
	 * @param container the {@link ItemStack} to try and drain
	 * @param network the {@link INetworkCache} to drain into
	 * @param action should this action be simulated
	 * @return the new ItemStack */
	public ItemStack drainFluidItemStack(ItemStack container, int toDrain, ILogisticsNetwork network, ActionType action) {
		return container;
	}

	/** fills the players current item with a specific fluid from the network
	 * @param player the player interacting
	 * @param cache the network to fill from
	 * @param toFill the {@link StoredFluidStack} to fill with */
	public void fillHeldItem(EntityPlayer player, ILogisticsNetwork cache, StoredFluidStack toFill) {
	}

	/** drains the players current item into the network
	 * @param player the player interacting
	 * @param cache the network to drain into */
	public void drainHeldItem(EntityPlayer player, ILogisticsNetwork cache, int toDrain) {
	}
}
