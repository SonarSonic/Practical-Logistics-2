package sonar.logistics.core.tiles.connections.data.network;

import com.google.common.collect.Lists;
import sonar.logistics.api.core.tiles.nodes.IEntityNode;
import sonar.logistics.api.core.tiles.nodes.INode;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.api.core.tiles.wireless.emitters.IDataEmitter;
import sonar.logistics.api.core.tiles.wireless.receivers.IDataReceiver;
import sonar.logistics.base.filters.ITransferFilteredTile;
import sonar.logistics.base.tiles.INetworkTile;

import java.util.ArrayList;

public class CacheHandler<T extends INetworkTile> {

	public Class<T> clazz;
	public CacheHandler(Class<T> clazz) {
		this.clazz = clazz;
	}

	public static final CacheHandler<IDataReceiver> RECEIVERS = new CacheHandler<IDataReceiver>(IDataReceiver.class) {};
	public static final CacheHandler<IDataEmitter> EMITTERS = new CacheHandler<IDataEmitter>(IDataEmitter.class) {};
	public static final CacheHandler<IListReader> READER = new CacheHandler<IListReader>(IListReader.class) {};
	public static final CacheHandler<INetworkTile> TILE = new CacheHandler<INetworkTile>(INetworkTile.class) {};
	public static final CacheHandler<ITransferFilteredTile> TRANSFER_NODES = new CacheHandler<ITransferFilteredTile>(ITransferFilteredTile.class) {};
	public static final CacheHandler<INode> NODES = new CacheHandler<INode>(INode.class) {};
	public static final CacheHandler<IEntityNode> ENTITY_NODES = new CacheHandler<IEntityNode>(IEntityNode.class){};
	public static final ArrayList<CacheHandler> handlers = Lists.newArrayList(RECEIVERS, EMITTERS, READER, TILE, NODES, ENTITY_NODES, TRANSFER_NODES);// RECEIVERS and EMITTERS should always come first so connected connections are considered by NODEs and TRANSFER NODEs


	public static ArrayList<CacheHandler> getValidCaches(INetworkTile tile) {
		ArrayList<CacheHandler> valid = new ArrayList<>();
		for (CacheHandler handler : CacheHandler.handlers) {
			if (handler.clazz.isInstance(tile)) {
				valid.add(handler);
			}
		}
		return valid;
	}
}