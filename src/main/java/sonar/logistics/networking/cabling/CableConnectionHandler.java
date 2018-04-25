package sonar.logistics.networking.cabling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import sonar.core.helpers.ListHelper;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.networks.EmptyLogisticsNetwork;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.events.NetworkEvent;

public class CableConnectionHandler extends AbstractConnectionHandler<IDataCable> {

	public static CableConnectionHandler instance() {
		return PL2.proxy.cableManager;
	}

	public List<IDataCable> updateRenders = new ArrayList<>();
	public List<Integer> changedNetworks = new ArrayList<>();
	public List<IDataCable> cableUpdates = new ArrayList<>();

	public Map<IDataCable, List<INetworkTile>> cable_tiles = new HashMap<>();
	public Map<IDataCable, List<IInfoProvider>> cable_providers = new HashMap<>();
	public Map<IInfoProvider, Integer> info_providers = new HashMap<>();

	public void removeAll() {
		super.removeAll();
		updateRenders.clear();
		changedNetworks.clear();
		cableUpdates.clear();
	}

	public void addRenderUpdate(IDataCable cable) {
		ListHelper.addWithCheck(updateRenders, cable);
	}

	public void doRenderUpdates() {
		if (!updateRenders.isEmpty()) {
			updateRenders.forEach(IDataCable::updateCableRenders);
			updateRenders.clear();
		}
	}

	public ILogisticsNetwork getOrCreateNetwork(int networkID) {
		return LogisticsNetworkHandler.instance().getOrCreateNetwork(networkID);
	}

	public ILogisticsNetwork getSubNetwork(IInfoProvider provider) {
		Integer connected_network = info_providers.get(provider);
		if (connected_network == null) {
			return EmptyLogisticsNetwork.INSTANCE;
		}
		return LogisticsNetworkHandler.instance().getNetwork(connected_network);
	}

	public void onDataCableRemove(IDataCable cable){
		List<INetworkTile> cached = cable_tiles.getOrDefault(cable, new ArrayList<>());
		for (INetworkTile tile : cached) {
			if (tile.getNetwork().isValid()) {
				MinecraftForge.EVENT_BUS.post(new NetworkEvent.DisconnectedTile(tile.getNetwork(), tile));
			}
		}
		List<IInfoProvider> cached_providers = cable_providers.getOrDefault(cable, new ArrayList<>());
		for (IInfoProvider tile : cached_providers) {
			ILogisticsNetwork subNetwork = getSubNetwork(tile);
			if (subNetwork.isValid()) {
				MinecraftForge.EVENT_BUS.post(new NetworkEvent.DisconnectedLocalProvider(subNetwork, tile));
				info_providers.remove(tile);
			}
		}
		cable_tiles.remove(cable);
		cable_providers.remove(cable);
	}
	
	public void updateConnectedTiles(IDataCable cable, ILogisticsNetwork network){
		List<INetworkTile> cached = cable_tiles.getOrDefault(cable, new ArrayList<>());
		List<INetworkTile> updated = CableHelper.getConnectedTiles(cable);

		// disconnect old connections
		for (INetworkTile tile : cached) {
			if (!updated.contains(tile) && tile.getNetwork().isValid()) {
				MinecraftForge.EVENT_BUS.post(new NetworkEvent.DisconnectedTile(tile.getNetwork(), tile));
			}
		}

		// connect new connections
		for (INetworkTile tile : updated) {
			if (tile.getNetwork() != network) {
				if (tile.getNetwork().isValid()) {
					MinecraftForge.EVENT_BUS.post(new NetworkEvent.DisconnectedTile(tile.getNetwork(), tile));
				}
				MinecraftForge.EVENT_BUS.post(new NetworkEvent.ConnectedTile(network, tile));
			}
		}

		// cache cables adjacent tiles
		cable_tiles.put(cable, updated);
	}
	
	public void updateInfoProviders(IDataCable cable, ILogisticsNetwork network){
		List<IInfoProvider> cached_providers = cable_providers.getOrDefault(cable, new ArrayList<>());
		List<IInfoProvider> updated_providers = CableHelper.getLocalMonitors(cable);

		// disconnect old local providers
		for (IInfoProvider tile : cached_providers) {
			ILogisticsNetwork subNetwork = getSubNetwork(tile);
			if (!updated_providers.contains(tile) && subNetwork.isValid()) {
				MinecraftForge.EVENT_BUS.post(new NetworkEvent.DisconnectedLocalProvider(subNetwork, tile));
				info_providers.remove(tile);
			}
		}

		// connect new local providers
		for (IInfoProvider tile : updated_providers) {
			ILogisticsNetwork subNetwork = getSubNetwork(tile);
			if (subNetwork.isValid() && subNetwork != network) {
				MinecraftForge.EVENT_BUS.post(new NetworkEvent.DisconnectedLocalProvider(subNetwork, tile));
			}
			if (!subNetwork.isValid() || subNetwork != network) {
				MinecraftForge.EVENT_BUS.post(new NetworkEvent.ConnectedLocalProvider(network, tile));
				info_providers.put(tile, network.getNetworkID());
			}
		}

		// cache cables's local providers
		cable_providers.put(cable, updated_providers);
	}
	
	public void doQueuedUpdates() {
		for (IDataCable cable : cableUpdates) {
			if (cable.getRegistryID() == -1) {
				onDataCableRemove(cable);
				continue;
			}
			ILogisticsNetwork network = getOrCreateNetwork(cable.getRegistryID());
			updateConnectedTiles(cable, network);
			updateInfoProviders(cable, network);
		}
		cableUpdates.clear();

		for (Integer id : changedNetworks) {
			List<IDataCable> cables = getConnections(id);
			if (cables.isEmpty()) {
				ILogisticsNetwork network = LogisticsNetworkHandler.instance().getNetwork(id);
				if (network.isValid()) {
					network.onNetworkRemoved();
				}
			}

		}
		changedNetworks.clear();
	}

	public void queueNetworkChange(int networkID) {
		ListHelper.addWithCheck(changedNetworks, networkID);
	}

	public void queueCableUpdate(IDataCable cable) {
		ListHelper.addWithCheck(cableUpdates, cable);
		addRenderUpdate(cable);
	}

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
		queueCableUpdate(added);
	}

	@Override
	public void onConnectionRemoved(int id, IDataCable removed) {
		queueCableUpdate(removed);
	}

	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {
		ListHelper.addWithCheck(changedNetworks, newNetworks);
	}

	@Override
	public int getNextAvailableID() {
		return LogisticsNetworkHandler.instance().getNextIdentity();
	}

	@Override
	public void addConnectionToNetwork(IDataCable add) {
		int networkID = addConnection(add);
		queueNetworkChange(networkID);
	}

	@Override
	public void removeConnectionFromNetwork(IDataCable remove) {
		removeConnection(remove);
		queueNetworkChange(remove.getRegistryID());
	}

}
