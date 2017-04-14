package sonar.logistics.connections.channels;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import sonar.logistics.PL2Config;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.connections.CacheHandler;

public abstract class DefaultNetworkChannels<H extends INetworkHandler> implements INetworkChannels<H> {

	private final CacheHandler[] caches;
	public final ILogisticsNetwork network;
	public final H handler;
	private Integer updateTicks = null;

	public DefaultNetworkChannels(H handler, ILogisticsNetwork network, CacheHandler... caches) {
		this.network = network;
		this.handler = handler;
		this.caches = caches;
		onCreated();
	}

	public ILogisticsNetwork getNetwork(){
		return network;
	}

	public H getHandler(){
		return handler;
	}

	@Override
	public CacheHandler[] getValidCaches() {
		return caches;
	}

	protected void updateTicks() {}

	private void tick() {
		if (updateTicks == null) {
			this.updateTicks = ThreadLocalRandom.current().nextInt(0, handler.updateRate() + 1);
			updateTicks();
		}
		if (this.updateTicks < handler.updateRate()) {
			updateTicks++;
			return;
		}
		this.updateTicks = 0;
		updateTicks();
	}

	@Override
	public void createChannelLists() {}

	@Override
	public void updateChannelLists() {
		tick();
	}

	@Override
	public void onDeleted() {}

	@Override
	public void addConnection(CacheHandler cache, INetworkListener connection) {}

	@Override
	public void removeConnection(CacheHandler cache, INetworkListener connection) {}

}