package sonar.logistics.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.IValidate;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.wireless.IEntityTransceiver;
import sonar.logistics.api.wireless.ITileTransceiver;
import sonar.logistics.networking.CacheHandler;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.ServerInfoHandler;

public class LogisticsHelper {

	public static boolean isPlayerUsingOperator(EntityPlayer player) {
		player.getHeldItemMainhand();
		return player.getHeldItemMainhand().getItem() instanceof IOperatorTool;
	}

	/** gets a list of all valid networks from the provided network ids */
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

	public List<InfoUUID> getConnectedUUIDS(List<IDisplay> displays) {
		ArrayList<InfoUUID> ids = new ArrayList<>();
		for (IDisplay display : displays) {
			DisplayGSI container = display.getGSI();
			container.forEachValidUUID(id -> {
				if (InfoUUID.valid(id) && !ids.contains(id))
					ids.add(id);
			});
		}
		return ids;
	}

	public List<IInfo> getInfoFromUUIDs(List<InfoUUID> ids) {
		List<IInfo> infoList = new ArrayList<>();
		for (InfoUUID id : ids) {
			ILogicListenable monitor = ServerInfoHandler.instance().getIdentityTile(id.getIdentity());
			if (monitor instanceof IInfoProvider) {
				IInfo info = ((IInfoProvider) monitor).getMonitorInfo(id.channelID);
				if (info != null)
					infoList.add(info);
			}
		}
		return infoList;
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
