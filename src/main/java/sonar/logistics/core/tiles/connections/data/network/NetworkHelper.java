package sonar.logistics.core.tiles.connections.data.network;

import net.minecraft.tileentity.TileEntity;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.utils.CacheType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class NetworkHelper {

	public static boolean forEachTileEntity(ILogisticsNetwork network, CacheType type, Predicate<NodeConnection> canUse, BiPredicate<BlockConnection, TileEntity> canContinue) {
		List<NodeConnection> connections = network.getConnections(type);
		for (NodeConnection entry : connections) {
			if (!canUse.test(entry))
				continue;
			if (entry instanceof BlockConnection) {
				BlockConnection connection = (BlockConnection) entry;
				TileEntity tile = connection.coords.getTileEntity();
				if (tile != null) {
					boolean bool = canContinue.test(connection, tile);
					if (!bool) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static List<ILogisticsNetwork> getAllNetworks(ILogisticsNetwork network, int networkType) {
		List<ILogisticsNetwork> networks = new ArrayList<>();
		addSubNetworks(networks, network, networkType);
		return networks;
	}

	public static void addSubNetworks(List<ILogisticsNetwork> building, ILogisticsNetwork network, int networkType) {
		building.add(network);
		List<ILogisticsNetwork> subNetworks = network.getListenerList().getListeners(networkType);
		for (ILogisticsNetwork sub : subNetworks) {
			if (sub.isValid() && !building.contains(sub)) {
				addSubNetworks(building, sub, networkType);
			}
		}
	}

	public static ILogisticsNetwork getNetwork(int networkID){
		return LogisticsNetworkHandler.instance().getNetwork(networkID);
	}
}
