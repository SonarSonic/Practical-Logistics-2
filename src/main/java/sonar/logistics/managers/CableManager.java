package sonar.logistics.managers;

import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.utils.Pair;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.helpers.CableHelper;

public class CableManager extends AbstractConnectionManager<IDataCable> {

	@Override
	public Pair<ConnectableType, Integer> getConnectionType(IDataCable source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		return CableHelper.getConnectionType(source, world, pos, dir, cableType);
	}

	public ILogisticsNetwork addCable(IDataCable cable) {
		return NetworkManager().getOrCreateNetwork(addConnection(cable));
	}

	public void removeCable(IDataCable cable) {
		super.removeConnection(cable);
	}

	@Override
	public void onNetworksConnected(int newID, int oldID) {
		NetworkManager().connectNetworks(oldID, newID);
	}

	@Override
	public void onConnectionAdded(int registryID, IDataCable added) {
		added.addConnections(NetworkManager().getOrCreateNetwork(registryID));
	}

	@Override
	public void onConnectionRemoved(int id, IDataCable added) {
		ILogisticsNetwork network = NetworkManager().getNetwork(id);
		added.removeConnections(network);
		network.removeConnections(); // ensure the CacheHandler empty the parts to remove.
	}

	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {
		for (int i : newNetworks) {
			ILogisticsNetwork network = NetworkManager().getNetwork(i);
			network.onCablesChanged();
		}
	}

}
