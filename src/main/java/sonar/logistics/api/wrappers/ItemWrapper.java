package sonar.logistics.api.wrappers;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import sonar.core.api.StorageSize;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.info.types.MonitoredItemStack;

public class ItemWrapper {
	
	/** used for getting the full list of Items at a given {@link INode}
	 * @param storedStacks current list of {@link StoredItemStack} to be added to
	 * @param storage currentStorageSize
	 * @param node {@link INode} to check from
	 * @return list of {@link StoredItemStack} on the network */
	public StorageSize getTileInventory(List<StoredItemStack> storedStacks, StorageSize storage, List<BlockConnection> connections) {
		return storage;
	}

	/** used for getting the full list of Items at a given {@link IEntityNode}
	 * @param storedStacks current list of {@link StoredItemStack} to be added to
	 * @param node {@link IEntityNode} to check from
	 * @return list of {@link StoredItemStack} on the network */
	public StorageSize getEntityInventory(List<StoredItemStack> storedStacks, StorageSize storage, List<Entity> entityList) {
		return storage;
	}

	/** used for adding Items to the network
	 * @param add {@link StoredItemStack} to add
	 * @param network the {@link ILogisticsNetwork} to add to
	 * @param action what type of action should be carried out
	 * @return remaining {@link StoredItemStack} (what wasn't added), can be null */
	public StoredItemStack addItems(StoredItemStack add, ILogisticsNetwork network, ActionType action) {
		return add;
	}

	public void addItemsFromPlayer(StoredItemStack add, EntityPlayer player, ILogisticsNetwork network, ActionType action) {}
	
	/** used for removing Items from the network
	 * @param remove {@link StoredItemStack} to remove
	 * @param network the {@link ILogisticsNetwork} to remove from
	 * @param action what type of action should be carried out
	 * @return remaining {@link StoredItemStack} (what wasn't removed), can be null */
	public StoredItemStack removeItems(StoredItemStack remove, ILogisticsNetwork network, ActionType action) {
		return remove;
	}

	public void removeItemsFromPlayer(StoredItemStack add, EntityPlayer player, ILogisticsNetwork network, ActionType action) {}
	
	/** gets the {@link StoredItemStack} in the given slot of the first valid inventory on the network, used by the Inventory Reader
	 * @param network the {@link ILogisticsNetwork} to get the stack from
	 * @param slot id of the slot to look for the stack in
	 * @return {@link StoredItemStack} of the ItemStack in the slot */
	public StoredItemStack getStack(ILogisticsNetwork network, int slot) {
		return null;
	}

	/** gets the {@link StoredItemStack} in the given slot of the entity connected to the {@link IEntityNode}
	 * @param node {@link IEntityNode} to check at
	 * @param slot id of the slot to look for the stack in
	 * @return {@link StoredItemStack} of the ItemStack in the slot */
	public StoredItemStack getEntityStack(ILogisticsNetwork node, int slot) {
		return null;
	}

	/** gets the {@link StoredItemStack} in the given slot of the tile connected to the {@link INode}
	 * @param network the {@link ILogisticsNetwork} to get stack from
	 * @param slot id of the slot to look for the stack in
	 * @return {@link StoredItemStack} of the ItemStack in the slot */
	public StoredItemStack getTileStack(ILogisticsNetwork network, int slot) {
		return null;
	}

	/** should NEVER be called on client, adds a StoredItemStack to a player inventory and sends changes with client
	 * @param add {@link StoredItemStack} to add
	 * @param player player to add to
	 * @param enderChest should change player Ender Chest or their normal inventory
	 * @param action what type of action should be carried out
	 * @return remaining {@link StoredItemStack} (what wasn't added), can be null */
	@Deprecated
	public StoredItemStack addStackToPlayer(StoredItemStack add, EntityPlayer player, boolean enderChest, ActionType action) {
		return add;
	}

	/** should NEVER be called on client, removes a StoredItemStack to a player inventory and sends changes with client
	 * @param remove {@link StoredItemStack} to remove
	 * @param player player to remove from
	 * @param enderChest should change player Ender Chest or their normal inventory
	 * @param action what type of action should be carried out
	 * @return remaining {@link StoredItemStack} (what wasn't removed), can be null */
	@Deprecated
	public StoredItemStack removeStackFromPlayer(StoredItemStack remove, EntityPlayer player, boolean enderChest, ActionType action) {
		return remove;
	}

	/** convenience method, adds the given stack to the player's inventory and returns what was/can be added.
	 * @param stack
	 * @param extractSize
	 * @param network the {@link ILogisticsNetwork} to remove from
	 * @param player
	 * @return the {@link StoredItemStack} to add to the player */
	//@Deprecated
	public StoredItemStack removeToPlayerInventory(StoredItemStack stack, long extractSize, ILogisticsNetwork network, EntityPlayer player, ActionType type) {
		return null;
	}

	/** convenience method, removes the given stack to the player's inventory and returns what was/can be added.
	 * @param stack
	 * @param extractSize
	 * @param network the {@link ILogisticsNetwork} to add to
	 * @param player
	 * @return the {@link StoredItemStack} to add to the player */
	public StoredItemStack addFromPlayerInventory(StoredItemStack stack, long extractSize, ILogisticsNetwork network, EntityPlayer player, ActionType type) {
		return null;
	}

	/** simple method to extract a given stack from a network
	 * @param cache the network to remove from
	 * @param stack the stack to remove
	 * @return the removed stack */
	public StoredItemStack extractItem(ILogisticsNetwork cache, StoredItemStack stack) {
		return null;
	}

	/** inserts all the items from the players inventory into the network which match the one in the given slot
	 * @param player the player who is inserting the items
	 * @param cache the network to add them to
	 * @param slot the slot of the item to be added */
	public void insertInventoryFromPlayer(EntityPlayer player, ILogisticsNetwork cache, int slotID) {}

	/** inserts an item into the given network from the players inventory from the given slot
	 * @param player the player who is inserting the items
	 * @param cache the network to add them to
	 * @param slot the slot to remove from */
	public void insertItemFromPlayer(EntityPlayer player, ILogisticsNetwork cache, int slot) {}

	public void dumpInventoryFromPlayer(EntityPlayer player, ILogisticsNetwork cache) {}

	public void dumpNetworkToPlayer(MonitoredList<MonitoredItemStack> items, EntityPlayer player, ILogisticsNetwork cache) {}
}
