package sonar.logistics.api.networks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.networking.CacheHandler;

/**the default version of ILogisticsNetwork when a tile is yet to be connected*/
public class EmptyLogisticsNetwork implements ILogisticsNetwork  {

	public static final EmptyLogisticsNetwork INSTANCE = new EmptyLogisticsNetwork();
	
	@Override
	public void onNetworkCreated() {}

	@Override
	public void onNetworkTick() {}

	@Override
	public void onNetworkRemoved() {}

	@Override
	public void onCablesChanged() {}

	@Override
	public void onCacheChanged(CacheHandler ...cache) {}

	@Override
	public boolean validateTile(INetworkListener listener) {
		return false;
	}

	@Override
	public void onConnectionChanged(INetworkListener tile) {}

	@Override
	public void addConnection(INetworkListener tile) {}

	@Override
	public void removeConnection(INetworkListener tile) {}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {}

	@Override
	public void removeLocalInfoProvider(IInfoProvider monitor) {}

	@Override
	public void onListenerAdded(ListenerTally<ILogisticsNetwork> tally) {}

	@Override
	public void onListenerRemoved(ListenerTally<ILogisticsNetwork> tally) {}

	@Override
	public void onSubListenableAdded(ISonarListenable<ILogisticsNetwork> listen) {}

	@Override
	public void onSubListenableRemoved(ISonarListenable<ILogisticsNetwork> listen) {}

	@Override
	public void sendConnectionsPacket(EntityPlayer player) {}

	@Override
	public ListenableList<ILogisticsNetwork> getListenerList() {
		return null;
	}
	
	@Override
	public List<NodeConnection> getConnections(CacheType cacheType) {
		return new ArrayList<>();
	}

	@Override
	public InfoChangeableList<MonitoredBlockCoords> createConnectionsList(CacheType cacheType) {
		return new InfoChangeableList();
	}

	@Override
	public <T> List<T> getCachedTiles(CacheHandler<T> handler, CacheType cacheType) {
		return new ArrayList<>();
	}

	@Override
	public <T extends INetworkChannels> T getNetworkChannels(Class<T> channelClass){
		return null;
	}

	@Override
	public List<IInfoProvider> getLocalInfoProviders() {
		return new ArrayList<>();
	}

	@Override
	public List<IInfoProvider> getGlobalInfoProviders() {
		return new ArrayList<>();
	}

	@Override
	public IInfoProvider getLocalInfoProvider() {
		return null;
	}
	
	@Override
	public boolean isValid() {
		return false;
	}
	
	@Override
	public int getNetworkID() {
		return 0;
	}

	@Override
	public <T extends INetworkChannels> T getOrCreateNetworkChannels(Class<T> channelClass) {
		return null;//this may cause problems lets hope not
	}

	@Override
	public void onConnectedNetworkCacheChanged(ILogisticsNetwork network) {}
}
