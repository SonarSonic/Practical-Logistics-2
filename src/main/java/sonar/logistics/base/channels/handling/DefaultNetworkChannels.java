package sonar.logistics.base.channels.handling;

import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.readers.channels.INetworkChannels;
import sonar.logistics.core.tiles.connections.data.network.CacheHandler;
import sonar.logistics.base.tiles.INetworkTile;

import java.util.concurrent.ThreadLocalRandom;

public abstract class DefaultNetworkChannels implements INetworkChannels {

	private final CacheHandler[] caches;
	public final ILogisticsNetwork network;
	private Integer updateTicks = null;

	public DefaultNetworkChannels(ILogisticsNetwork network, CacheHandler... caches) {
		this.network = network;
		this.caches = caches;
		onCreated();
	}

	public ILogisticsNetwork getNetwork(){
		return network;
	}
	@Override
	public CacheHandler[] getValidCaches() {
		return caches;
	}

	protected void tickChannels() {}

	private void tick() {
		if (updateTicks == null) {
			this.updateTicks = ThreadLocalRandom.current().nextInt(0, getUpdateRate() + 1);
			tickChannels();
		}
		if (this.updateTicks < getUpdateRate()) {
			updateTicks++;
			return;
		}
		this.updateTicks = 0;
		tickChannels();
	}
	
	public abstract int getUpdateRate();

	@Override
	public void onChannelsChanged() {}

	@Override
	public void updateChannel() {
		tick();
	}

	@Override
	public void onDeleted() {}

	@Override
	public void addConnection(INetworkTile connection) {}

	@Override
	public void removeConnection(INetworkTile connection) {}

}