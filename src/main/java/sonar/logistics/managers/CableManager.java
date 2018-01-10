package sonar.logistics.managers;

import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.utils.Pair;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.common.multiparts2.cables.CableConnectionHandler;
import sonar.logistics.helpers.CableHelper;

public class CableManager extends AbstractConnectionManager<IDataCable> {

	@Override
	public Pair<ConnectableType, Integer> getConnectionType(IDataCable source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		return CableHelper.getCableConnection(source, world, pos, dir, cableType);
	}
	@Override
	public void onNetworksConnected(int newID, int oldID) {
		NetworkManager().connectNetworks(oldID, newID);
	}

	@Override
	public void onConnectionAdded(int registryID, IDataCable added) {
		CableConnectionHandler.addAllConnectionsToNetwork(added, NetworkManager().getOrCreateNetwork(registryID));		
	}

	@Override
	public void onConnectionRemoved(int id, IDataCable added) {
		ILogisticsNetwork network = NetworkManager().getNetwork(id);
		CableConnectionHandler.removeAllConnectionsFromNetwork(added, network);
		network.removeConnections(); // ensure the CacheHandler empty the parts to remove.
	}
	
	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {
		for (int i : newNetworks) {
			ILogisticsNetwork network = NetworkManager().getNetwork(i);
			network.onCablesChanged();
		}
	}

	@Override
	public void addConnectionToNetwork(IDataCable add) {
		CableConnectionHandler.addCableToNetwork(add);
	}

	@Override
	public void removeConnectionToNetwork(IDataCable remove) {
		CableConnectionHandler.removeCableFromNetwork(remove);		
	}

}
