package sonar.logistics.api.connecting;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.IInfoProvider;

/** an implementation of {@link INetworkCache} that acts as an Empty Network, when working with networks the INSTANCE of this should be returned instead of null */
public class EmptyNetworkCache implements INetworkCache {

	public static final EmptyNetworkCache INSTANCE = EmptyNetworkCache.createEmptyCache();

	private EmptyNetworkCache() {
	}

	public static EmptyNetworkCache createEmptyCache() {
		return new EmptyNetworkCache();
	}
	
	@Override
	public ArrayList<NodeConnection> getConnectedChannels(boolean includeChannels) {
		return Lists.newArrayList();
	}

	@Override
	public <T extends IWorldPosition> ArrayList<T> getConnections(Class<T> classType, boolean includeChannels) {
		return Lists.newArrayList();
	}

	@Override
	public <T extends IWorldPosition> T getFirstConnection(Class<T> classType) {
		return null;
	}

	@Override
	public int getNetworkID() {
		return -1;
	}

	@Override
	public ArrayList<Integer> getConnectedNetworks(ArrayList<Integer> networks) {
		return networks;
	}

	@Override
	public boolean isFakeNetwork() {
		return true;
	}

	@Override
	public void addLocalInfoProvider(IInfoProvider monitor) {
	}

	@Override
	public IInfoProvider getLocalInfoProvider() {
		return null;
	}

	@Override
	public void markDirty(RefreshType type) {
	}

	@Override
	public ArrayList<IInfoProvider> getLocalInfoProviders() {
		return new ArrayList();
	}

}
