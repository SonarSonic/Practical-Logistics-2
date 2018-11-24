package sonar.logistics.core.tiles.connections.data.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.readers.IInfoProvider;
import sonar.logistics.api.core.tiles.readers.channels.INetworkChannels;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;

import java.util.ArrayList;
import java.util.List;

/**the default version of ILogisticsNetwork when a tile is yet to be connected*/
public class EmptyLogisticsNetwork implements ILogisticsNetwork {

	public static final EmptyLogisticsNetwork INSTANCE = new EmptyLogisticsNetwork();
	
	@Override
	public void onNetworkCreated() {}

	@Override
	public void onNetworkTick() {}

	@Override
	public void onNetworkRemoved() {}

	@Override
	public boolean validateTile(INetworkTile listener) {
		return false;
	}

	@Override
	public void addConnection(INetworkTile tile) {}

	@Override
	public void removeConnection(INetworkTile tile) {}

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
	public <T extends INetworkTile> List<T> getCachedTiles(CacheHandler<T> handler, CacheType cacheType) {
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
	public IItemHandler getNetworkItemHandler() {
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
}
