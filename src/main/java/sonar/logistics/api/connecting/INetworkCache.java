package sonar.logistics.api.connecting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.IInfoProvider;

/** implemented on Logistics Network Caches, used for retrieving info about */
public interface INetworkCache {

	/** used to get the first external block connected to the network.
	 * 
	 * @param includeChannels normally true, false if you are retrieving blocks from multiple connected networks, which have already been logged
	 * @return a {@link Entry} of {@link BlockCoords} paired with {@link EnumFacing} */
	@Deprecated
	public BlockConnection getExternalBlock(boolean includeChannels);

	/** @param includeChannels normally true, false if you are retrieving blocks from multiple connected networks, which have already been logged
	 * @return the {@link LinkedHashMap} of {@link BlockCoords} paired with {@link EnumFacing} */
	public ArrayList<BlockConnection> getExternalBlocks(boolean includeChannels);

	/** @param includeChannels normally true, false if you are retrieving blocks from multiple connected networks, which have already been logged
	 * @return the {@link LinkedHashMap} of {@link BlockCoords} paired with {@link EnumFacing} */
	public ArrayList<Entity> getExternalEntities(boolean includeChannels);

	/** gets the full list of Cached Coordinates for a given {@link CacheType}.
	 * 
	 * @param type the {@link CacheType} you wish to retrieve.
	 * @param includeChannels normally true, false if you are retrieving blocks from multiple connected networks, which have already been logged
	 * @return the {@link ArrayList}<{@link BlockCoords}> requested */
	public <T extends IWorldPosition> ArrayList<T> getConnections(Class<T> classType, boolean includeChannels);

	/** convenience method for getting the BlockCoords of the first connection from the given {@link CacheType}
	 * 
	 * @param type the {@link CacheType} you wish to retrieve.
	 * @return the {@link BlockCoords} requested, may be null */
	public <T extends IWorldPosition> T getFirstConnection(Class<T> classType);

	/** @return the networkID, related to the id of the Cable Network */
	public int getNetworkID();

	/** used when getting full list of networks associated with this one, via {@link IChannelProvider}}
	 * 
	 * @param networks an ArrayList containing all current network IDs, IDs shouldn't be removed - unless in very rare circumstances
	 * @return same ArrayList with the Networks associated with this network added */
	public ArrayList<Integer> getConnectedNetworks(ArrayList<Integer> networks);

	/** quick method to check this network can be used/edited typically only and EmptyNetworkCache would return true */
	public boolean isFakeNetwork();

	public void addLocalInfoProvider(IInfoProvider monitor);

	public IInfoProvider getLocalInfoProvider();

	public ArrayList<IInfoProvider> getLocalInfoProviders();

	public void markDirty(RefreshType type);
}
