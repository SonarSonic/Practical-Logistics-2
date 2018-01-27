package sonar.logistics.networking;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.tileentity.TileEntity;
import sonar.logistics.PL2;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.viewers.ILogicListenable;

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

	public static ILogisticsNetwork getNetwork(int networkID){
		return PL2.getNetworkManager().getNetwork(networkID);
	}

	public static ILogicListenable getIdentityTile(int identity){
		return PL2.getServerManager().getIdentityTile(identity);
	}
}
