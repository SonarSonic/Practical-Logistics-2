package sonar.logistics.api.connecting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.ILogicMonitor;

/** an implementation of {@link INetworkCache} that acts as an Empty Network, when working with networks the INSTANCE of this should be returned instead of null */
public class EmptyNetworkCache implements INetworkCache {

	public static final EmptyNetworkCache INSTANCE = EmptyNetworkCache.createEmptyCache();

	private EmptyNetworkCache() {}

	public static EmptyNetworkCache createEmptyCache() {
		return new EmptyNetworkCache();
	}

	@Override
	public NodeConnection getExternalBlock(boolean includeChannels) {
		return null;
	}

	@Override
	public ArrayList<NodeConnection> getExternalBlocks(boolean includeChannels) {
		return Lists.newArrayList();
	}

	@Override
	public ArrayList<Entity> getExternalEntities(boolean includeChannels) {
		return Lists.newArrayList();
	}

	@Override
	public <T extends IWorldPosition> ArrayList<T> getConnections(Class<T> classType, boolean includeChannels){
		return Lists.newArrayList();
	}

	@Override
	public <T extends IWorldPosition> T getFirstConnection(Class<T> classType){
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
	public void addLocalMonitor(ILogicMonitor monitor) {}

	@Override
	public ILogicMonitor getLocalMonitor() {
		return null;
	}

	@Override
	public void markDirty(RefreshType type) {}

	@Override
	public ArrayList<ILogicMonitor> getLocalMonitors() {
		return new ArrayList();
	}

}
