package sonar.logistics.connections;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import sonar.logistics.api.connecting.INetworkListener;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.readers.IListReader;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.connections.managers.EmitterManager;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;

public abstract class CacheHandler<T> {

	public static final CacheHandler<IDataReceiver> RECEIVERS = new CacheHandler<IDataReceiver>(IDataReceiver.class) {

		@Override
		public void update(LogisticsNetwork network, List<IDataReceiver> connections) {
			network.updateSubNetworks();
			network.updateGlobalChannels();
			network.updateCoordsList();
		}

	};

	public static final CacheHandler<IDataEmitter> EMITTERS = new CacheHandler<IDataEmitter>(IDataEmitter.class) {

		@Override
		public void update(LogisticsNetwork network, List<IDataEmitter> connections) {
			//network.updateWatchingNetworks();
		}

		public void onConnectionAdded(LogisticsNetwork network, IDataEmitter emitter) {
			EmitterManager.addEmitter(emitter);
		}

		public void onConnectionRemoved(LogisticsNetwork network, IDataEmitter emitter) {
			EmitterManager.removeEmitter(emitter);
		}
	};

	public static final CacheHandler<IListReader> READER = new CacheHandler<IListReader>(IListReader.class) {

		public void update(LogisticsNetwork network, List<IListReader> connections) {
		}

		public void onConnectionAdded(LogisticsNetwork network, IListReader connection) {
			for (LogicMonitorHandler handler : connection.getValidHandlers()) {
				network.monitorInfo.putIfAbsent(handler, new ArrayList());
				if (!network.monitorInfo.get(handler).contains(connection)) {
					network.monitorInfo.get(handler).add(connection);
				}
				network.compileConnectionList(handler);
			}
			network.updateCoordsList();
		}

		public void onConnectionRemoved(LogisticsNetwork network, IListReader connection) {
			for (LogicMonitorHandler handler : connection.getValidHandlers()) {
				network.monitorInfo.get(handler).remove(connection);
				network.compileConnectionList(handler);
			}
			network.updateCoordsList();

		}
	};

	public static final CacheHandler<INetworkListener> TILE = new CacheHandler<INetworkListener>(INetworkListener.class) {

		@Override
		public void onConnectionAdded(LogisticsNetwork network, INetworkListener connection) {
			connection.onNetworkConnect(network);
		}

		@Override
		public void onConnectionRemoved(LogisticsNetwork network, INetworkListener connection) {
			connection.onNetworkDisconnect(network);
		}
	};

	public static final CacheHandler<ITransferFilteredTile> TRANSFER_NODES = new CacheHandler<ITransferFilteredTile>(ITransferFilteredTile.class) {

		@Override
		public void update(LogisticsNetwork network, List<ITransferFilteredTile> connections) {

		}

	};

	public static final CacheHandler<IConnectionNode> NODES = new CacheHandler<IConnectionNode>(IConnectionNode.class) {

		@Override
		public void update(LogisticsNetwork network, List<IConnectionNode> connections) {
			network.updateChannels();
			network.updateCoordsList();
		}

	};

	public static final ArrayList<CacheHandler> handlers = Lists.newArrayList(RECEIVERS, EMITTERS, READER, TILE, NODES, TRANSFER_NODES);// RECEIVERS and EMITTERS should always come first so connected networks are considered by NODEs and TRANSFER NODEs

	public Class<T> clazz;

	public CacheHandler(Class<T> clazz) {
		this.clazz = clazz;
	}

	public void update(LogisticsNetwork network, List<T> connections) {
	}

	public void onConnectionAdded(LogisticsNetwork network, T connection) {
	}

	public void onConnectionRemoved(LogisticsNetwork network, T connection) {
	}

	public static ArrayList<CacheHandler> getValidCaches(INetworkListener tile) {
		ArrayList<CacheHandler> valid = new ArrayList();
		for (CacheHandler handler : CacheHandler.handlers) {
			if (handler.clazz.isInstance(tile)) {
				valid.add(handler);
			}
		}
		return valid;
	}
}