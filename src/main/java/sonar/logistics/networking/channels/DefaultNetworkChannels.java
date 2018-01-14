package sonar.logistics.networking.channels;

import java.util.concurrent.ThreadLocalRandom;

import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.networking.CacheHandler;

public abstract class DefaultNetworkChannels<H extends INetworkHandler> implements INetworkChannels<H> {

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

	protected void updateTicks() {}

	private void tick() {
		if (updateTicks == null) {
			this.updateTicks = ThreadLocalRandom.current().nextInt(0, getUpdateRate() + 1);
			updateTicks();
		}
		if (this.updateTicks < getUpdateRate()) {
			updateTicks++;
			return;
		}
		this.updateTicks = 0;
		updateTicks();
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