package sonar.logistics.networking.cabling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.ListHelper;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.networks.EmptyLogisticsNetwork;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.events.LogisticsEventHandler;
import sonar.logistics.networking.events.NetworkEvent;
import sonar.logistics.networking.events.NetworkPartEvent;

public class CableConnectionHandler extends AbstractConnectionHandler<IDataCable> {

	public static CableConnectionHandler instance() {
		return PL2.proxy.cableManager;
	}

	/* public Map<IDataCable, List<INetworkTile>> connected = new HashMap<>(); public Map<IDataCable, List<INetworkTile>> disconnected = new HashMap<>(); public Map<IDataCable, List<IInfoProvider>> connected_providers = new HashMap<>(); public Map<IDataCable, List<IInfoProvider>> disconnected_providers = new HashMap<>(); */
	public List<IDataCable> updateRenders = new ArrayList<>();
	public List<Integer> changedNetworks = new ArrayList<>();
	public List<IDataCable> cableUpdates = new ArrayList<>();

	public Map<IDataCable, List<INetworkTile>> cable_tiles = new HashMap<>();
	public Map<IDataCable, List<IInfoProvider>> cable_providers = new HashMap<>();
	public Map<IInfoProvider, Integer> info_providers = new HashMap<>();

	public void removeAll() {
		super.removeAll();
		cable_tiles.clear();
		cable_providers.clear();
		info_providers.clear();
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

	public void doQueuedUpdates() {
		for (IDataCable cable : cableUpdates) {
			if (cable.getRegistryID() == -1) {
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
				continue;
			}
			ILogisticsNetwork network = getOrCreateNetwork(cable.getRegistryID());
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
		cableUpdates.clear();

		for (Integer id : changedNetworks) {
			List<IDataCable> cables = getConnections(id);
			if (cables.isEmpty()) {
				ILogisticsNetwork network = LogisticsNetworkHandler.instance().getNetwork(id);
				if (network != null) {
					network.onNetworkRemoved();
				}
			} else {
				ILogisticsNetwork network = getOrCreateNetwork(id);
				network.onCablesChanged();
			}
			// update

		}
		changedNetworks.clear();
	}

	public void queueNetworkChange(int networkID) {
		ListHelper.addWithCheck(changedNetworks, networkID);
	}

	public void queueCableUpdate(IDataCable cable) {
		ListHelper.addWithCheck(cableUpdates, cable);
	}

	public void queueNetworkTileAddition(IDataCable cable, INetworkTile tile) {
		queueCableUpdate(cable);
	}

	public void queueNetworkTileRemoval(IDataCable cable, INetworkTile tile) {
		queueCableUpdate(cable);
	}

	public void queueLocalProviderAddition(IDataCable cable, IInfoProvider tile) {
		queueCableUpdate(cable);
	}

	public void queueLocalProviderRemoval(IDataCable cable, IInfoProvider tile) {
		queueCableUpdate(cable);
	}

	/* public void queueNetworkTileAddition(IDataCable cable, INetworkTile tile) { List<INetworkTile> added = connected.computeIfAbsent(cable, FunctionHelper.ARRAY); List<INetworkTile> removed = disconnected.computeIfAbsent(cable, FunctionHelper.ARRAY); ListHelper.addWithCheck(added, tile); removed.remove(tile); } public void queueNetworkTileRemoval(IDataCable cable, INetworkTile tile) { List<INetworkTile> added = connected.computeIfAbsent(cable, FunctionHelper.ARRAY); List<INetworkTile> removed = disconnected.computeIfAbsent(cable, FunctionHelper.ARRAY); added.remove(tile); ListHelper.addWithCheck(removed, tile); } public void queueLocalProviderAddition(IDataCable cable, IInfoProvider tile) { List<INetworkTile> added = connected_providers.computeIfAbsent(cable, FunctionHelper.ARRAY); List<INetworkTile> removed = disconnected_providers.computeIfAbsent(cable, FunctionHelper.ARRAY); ListHelper.addWithCheck(added, tile); removed.remove(tile); } public void queueLocalProviderRemoval(IDataCable cable, IInfoProvider tile) { List<INetworkTile> added = connected_providers.computeIfAbsent(cable, FunctionHelper.ARRAY); List<INetworkTile> removed = disconnected_providers.computeIfAbsent(cable, FunctionHelper.ARRAY); added.remove(tile); ListHelper.addWithCheck(removed, tile); } */

	//// MULTIPARTS \\\\

	public void onNeighbourMultipartAdded(IDataCable cable, IPartInfo part, IPartInfo otherPart) {
		if (otherPart.getTile() != null && otherPart.getTile() instanceof INetworkTile) {
			if(otherPart.getSlot() instanceof EnumFaceSlot){
				onNeighbourAdded(cable, ((INetworkTile) otherPart.getTile()), ((EnumFaceSlot) otherPart.getSlot()).getFacing(), true);
			}else if(otherPart.getSlot() instanceof EnumDisplayFaceSlot){
				onNeighbourAdded(cable, ((INetworkTile) otherPart.getTile()), ((EnumDisplayFaceSlot) otherPart.getSlot()).getFacing(), true);				
			}
		}
	}

	public void onNeighbourMultipartRemoved(IDataCable cable, IPartInfo part, IPartInfo otherPart) {
		if (otherPart.getTile() != null && otherPart.getTile() instanceof INetworkTile) {			
			if(otherPart.getSlot() instanceof EnumFaceSlot){
				onNeighbourRemoved(cable, ((INetworkTile) otherPart.getTile()), ((EnumFaceSlot) otherPart.getSlot()).getFacing(), true);
			}else if(otherPart.getSlot() instanceof EnumDisplayFaceSlot){
				onNeighbourRemoved(cable, ((INetworkTile) otherPart.getTile()), ((EnumDisplayFaceSlot) otherPart.getSlot()).getFacing(), true);				
			}
		}
	}

	//// TILES \\\\

	public void onNeighbourBlockStateChanged(IDataCable cable, BlockPos pos, BlockPos neighbor) {
		addRenderUpdate(cable);
	}

	public void onNeighbourTileEntityChanged(IDataCable cable, BlockPos pos, BlockPos neighbor) {
		CableHelper.getLocalMonitors(cable).forEach(m -> queueLocalProviderAddition(cable, m));
		addRenderUpdate(cable);
	}

	//// NETWORK TILES \\\\

	public void onNeighbourAdded(IDataCable cable, INetworkTile networkTile, EnumFacing cableSide, boolean internal) {
		if (cable.canConnect(networkTile.getNetworkID(), cable.getConnectableType(), cableSide, internal).canConnect()) {
			addConnectionToNetwork(cable, networkTile);
		}
	}

	public void onNeighbourRemoved(IDataCable cable, INetworkTile networkTile, EnumFacing cableSide, boolean internal) {
		if (networkTile.getNetworkID() == cable.getRegistryID()) {
			removeConnectionFromNetwork(cable, networkTile);
		}
	}

	//// ADD/REMOVE CABLES \\\\

	public void addCableToNetwork(IDataCable cable) {
		int networkID = addConnection(cable);
		queueNetworkChange(cable.getRegistryID());
	}

	public void removeCableFromNetwork(IDataCable cable) {
		removeConnection(cable);
		queueNetworkChange(cable.getRegistryID());
	}

	//// ADD/REMOVE CONNECTIONS \\\\

	public void addConnectionToNetwork(IDataCable cable, INetworkTile tile) {
		queueNetworkTileAddition(cable, tile);
		addRenderUpdate(cable);
	}

	public void removeConnectionFromNetwork(IDataCable cable, INetworkTile tile) {
		queueNetworkTileRemoval(cable, tile);
		addRenderUpdate(cable);
	}

	/** called only by the logistics network to move connections from network to network */

	public void addAllConnectionsToNetwork(IDataCable cable, int networkID) {
		CableHelper.getConnectedTiles(cable).forEach(t -> queueNetworkTileAddition(cable, t));
		CableHelper.getLocalMonitors(cable).forEach(m -> queueLocalProviderAddition(cable, m));
	}

	/** called only by the logistics network to move connections from network to network */

	public void removeAllConnectionsFromNetwork(IDataCable cable, int networkID) {
		CableHelper.getConnectedTiles(cable).forEach(t -> {
			queueNetworkTileRemoval(cable, t);
		});
	}

	/** abstract connection handler */

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
		//addAllConnectionsToNetwork(added, registryID);
		queueCableUpdate(added);
	}

	@Override
	public void onConnectionRemoved(int id, IDataCable removed) {
		//removeAllConnectionsFromNetwork(removed, id);
		queueCableUpdate(removed);
	}

	@Override
	public void onNetworksDisconnected(List<Integer> newNetworks) {
		ListHelper.addWithCheck(changedNetworks, newNetworks);
	}

	@Override
	public void addConnectionToNetwork(IDataCable add) {
		addCableToNetwork(add);
	}

	@Override
	public void removeConnectionFromNetwork(IDataCable remove) {
		removeCableFromNetwork(remove);
	}

}
