package sonar.logistics.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.api.wireless.IEntityTransceiver;
import sonar.logistics.api.wireless.ITileTransceiver;
import sonar.logistics.api.wireless.ITransceiver;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.monitoring.MonitoredList;

public class LogisticsHelper {

	public static boolean isPlayerUsingOperator(EntityPlayer player) {
		if (player.getHeldItemMainhand() != null) {
			return player.getHeldItemMainhand().getItem() instanceof IOperatorTool;
		}
		return false;
	}

	public static List<ILogisticsNetwork> getNetworks(List<Integer> ids) {
		List<ILogisticsNetwork> networks = new ArrayList();
		ids.forEach(id -> PL2.getNetworkManager().getNetwork(id));
		return networks;
	}

	public static HashMap<CacheHandler, ArrayList> getCachesMap() {
		HashMap<CacheHandler, ArrayList> connections = new HashMap();
		CacheHandler.handlers.forEach(classType -> connections.put(classType, new ArrayList()));
		return connections;
	}

	public static NodeConnection getTransceiverNode(ILogicTile source, ItemStack stack) {
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

	public static void addConnectedNetworks(ILogisticsNetwork main, IDataReceiver receiver) {
		ArrayList<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			ILogisticsNetwork sub = PL2.getNetworkManager().getNetwork(networkID);
			if (!sub.isFakeNetwork()) {
				sub.getListenerList().addListener(main, ILogisticsNetwork.WATCHING_NETWORK);
			}
		});
	}

	public static ArrayList<NodeConnection> sortNodeConnections(ArrayList<NodeConnection> channels, List<IConnectionNode> nodes) {
		nodes.forEach(n -> {
			if (n.isValid())
				n.addConnections(channels);
		});
		return NodeConnection.sortConnections(channels);
	}

}
