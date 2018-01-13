package sonar.logistics.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.info.render.IInfoContainer;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.wireless.IEntityTransceiver;
import sonar.logistics.api.wireless.ITileTransceiver;
import sonar.logistics.common.multiparts2.displays.TileAbstractDisplay;
import sonar.logistics.connections.CacheHandler;

public class LogisticsHelper {

	public static boolean isPlayerUsingOperator(EntityPlayer player) {
		if (player.getHeldItemMainhand() != null) {
			return player.getHeldItemMainhand().getItem() instanceof IOperatorTool;
		}
		return false;
	}

	/** gets a list of all valid networks from the provided network ids */
	public static List<ILogisticsNetwork> getNetworks(List<Integer> ids) {
		List<ILogisticsNetwork> networks = Lists.newArrayList();
		ids.forEach(id -> {
			ILogisticsNetwork network = PL2.getNetworkManager().getNetwork(id);
			if (network != null && network.isValid()) {
				networks.add(network);
			}
		});
		return networks;
	}

	/** creates a fresh HashMap with all CacheHandlers with ArrayLists placed */
	public static Map<CacheHandler, List> getCachesMap() {
		Map<CacheHandler, List> connections = Maps.newHashMap();
		CacheHandler.handlers.forEach(classType -> connections.put(classType, Lists.newArrayList()));
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

	public static NodeConnection getTransceiverNode(INetworkTile source, ItemStack stack) {
		if (stack.getItem() instanceof ITileTransceiver) {
			ITileTransceiver trans = (ITileTransceiver) stack.getItem();
			return new BlockConnection(source, trans.getCoords(stack), trans.getDirection(stack));
		}
		if (stack.getItem() instanceof IEntityTransceiver) {
			IEntityTransceiver trans = (IEntityTransceiver) stack.getItem();
			UUID uuid = trans.getEntityUUID(stack);
			if (uuid != null) {
				for (Entity entity : source.getCoords().getWorld().getLoadedEntityList()) {
					if (entity.getPersistentID().equals(uuid)) {
						return new EntityConnection(source, entity);
					}
				}
			}
		}
		return null;

	}

	public List<InfoUUID> getConnectedUUIDS(List<IDisplay> displays) {
		ArrayList<InfoUUID> ids = Lists.newArrayList();
		for (IDisplay display : displays) {
			IInfoContainer container = display.container();
			container.forEachValidUUID(id -> {
				if (!ids.contains(id))
					ids.add(id);
			});
		}
		return ids;
	}

	public List<IInfo> getInfoFromUUIDs(List<InfoUUID> ids) {
		List<IInfo> infoList = Lists.newArrayList();
		for (InfoUUID id : ids) {
			ILogicListenable monitor = CableHelper.getMonitorFromIdentity(id.getIdentity(), false);
			if (monitor != null && monitor instanceof IInfoProvider) {
				IInfo info = ((IInfoProvider) monitor).getMonitorInfo(id.channelID);
				if (info != null)
					infoList.add(info);
			}
		}
		return infoList;
	}

	public static List<ILogicListenable> getLocalProviders(List<ILogicListenable> viewables, IBlockAccess world, BlockPos pos, TileAbstractDisplay part) {
		ILogisticsNetwork networkCache = part.getNetwork();		
		Optional<IMultipartTile> connectedPart = SonarMultipartHelper.getMultipartTile(world, pos, EnumFaceSlot.fromFace(part.face), tile -> true);		
		if (connectedPart.isPresent() && connectedPart.get() instanceof IInfoProvider) {
			if (!viewables.contains((IInfoProvider) connectedPart.get())) {
				viewables.add((IInfoProvider) connectedPart.get());
			}
		} else {
			for (IInfoProvider monitor : networkCache.getLocalInfoProviders()) {
				if (!viewables.contains(monitor)) {
					viewables.add(monitor);
				}
			}
		}
		return viewables;
	}

	public static List<NodeConnection> sortNodeConnections(List<NodeConnection> channels, List<INode> nodes) {
		nodes.stream().filter(n -> n.isValid()).forEach(n -> n.addConnections(channels));
		return NodeConnection.sortConnections(channels);
	}

}
