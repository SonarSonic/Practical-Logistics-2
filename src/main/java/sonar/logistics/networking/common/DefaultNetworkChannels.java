package sonar.logistics.networking.common;

import sonar.logistics.api.networking.ILogisticsNetwork;
import sonar.logistics.api.networking.INetworkChannels;
import sonar.logistics.api.networking.INetworkListener;
import sonar.logistics.networking.CacheHandler;

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
	public void addConnection(CacheHandler cache, INetworkListener connection) {}

	@Override
	public void removeConnection(CacheHandler cache, INetworkListener connection) {}

}