package sonar.logistics.base.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.IValidate;
import sonar.logistics.api.core.items.operator.IOperatorTool;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.nodes.INode;
import sonar.logistics.api.core.tiles.wireless.transceivers.IEntityTransceiver;
import sonar.logistics.api.core.tiles.wireless.transceivers.ITileTransceiver;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.core.tiles.connections.data.network.CacheHandler;
import sonar.logistics.core.tiles.connections.data.network.LogisticsNetworkHandler;

import java.util.*;

public class LogisticsHelper {

	public static boolean isPlayerUsingOperator(EntityPlayer player) {
		return player.getHeldItemMainhand().getItem() instanceof IOperatorTool;
	}

	/** gets a list of all valid connections from the provided handling ids */
	public static List<ILogisticsNetwork> getNetworks(List<Integer> ids) {
		List<ILogisticsNetwork> networks = new ArrayList<>();
		ids.forEach(id -> {
			ILogisticsNetwork network = LogisticsNetworkHandler.instance().getNetwork(id);
			if (network.isValid()) {
				networks.add(network);
			}
		});
		return networks;
	}

	/** creates a fresh HashMap with all CacheHandlers with ArrayLists placed */
	public static Map<CacheHandler, List> getCachesMap() {
		Map<CacheHandler, List> connections = new HashMap<>();
		CacheHandler.handlers.forEach(classType -> connections.put(classType, new ArrayList<>()));
		return connections;
	}

	/** creates a new channel instance of the type provided, requires the constructor to only need the ILogisticsNetwork variable */
	public static <T> T getChannelInstance(Class<T> channelType, ILogisticsNetwork network) {
		try {
			return channelType.getConstructor(ILogisticsNetwork.class).newInstance(network);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static NodeConnection getTransceiverNode(INetworkTile source, World world, ItemStack stack) {
		if (stack.getItem() instanceof ITileTransceiver) {
			ITileTransceiver trans = (ITileTransceiver) stack.getItem();
			return new BlockConnection(source, trans.getCoords(stack), trans.getDirection(stack));
		}
		if (stack.getItem() instanceof IEntityTransceiver) {
			IEntityTransceiver trans = (IEntityTransceiver) stack.getItem();
			UUID uuid = trans.getEntityUUID(stack);
			if (uuid != null) {
				for (Entity entity : world.getLoadedEntityList()) {
					if (entity.getPersistentID().equals(uuid)) {
						return new EntityConnection(source, entity);
					}
				}
			}
		}
		return null;

	}

	public static List<NodeConnection> sortNodeConnections(List<NodeConnection> channels, List<INode> nodes) {
		nodes.stream().filter(IValidate::isValid).forEach(n -> n.addConnections(channels));
		return NodeConnection.sortConnections(channels);
	}

	public static ItemStack getCoordItem(BlockCoords coords, World world) {
		TileEntity tile = coords.getTileEntity(world);
		IBlockState state = coords.getBlockState(world);
		ItemStack stack = coords.getBlock(world).getItem(world, coords.getBlockPos(), state);
		if (stack.isEmpty()) {
			stack = new ItemStack(Item.getItemFromBlock(state.getBlock()));
		}
		return stack;
	}

}
