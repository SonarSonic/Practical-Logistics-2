package sonar.logistics.api.tiles.cable;

import net.minecraft.util.EnumFacing;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.render.ICableRenderer;
import sonar.logistics.api.tiles.IConnectable;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.IListReader;

/** implemented on Tile Entities and Forge Multipart parts which are cables */
public interface IDataCable extends ICableRenderer, IWorldPosition, IConnectable {

	/** can be called by a {@link IInfoProvider}, only for special situations if it cannot be detected by the {@link IDataCable}
	 * <p>
	 * A "Local Provider" is a {@link IInfoProvider} which isn't directly connected to the network but one side is still providing info to the network these local providers are typically {@link IListReader}s. This is how the {@link IDisplay}s find connectable readers.
	 * @param tile the info provider
	 * @param face the face it is connected to */
	public void onLocalProviderAdded(IInfoProvider tile, EnumFacing face);

	/** can be called by a {@link IInfoProvider}, only for special situations if it cannot be detected by the {@link IDataCable}
	 * <p>
	 * A "Local Provider" is a {@link IInfoProvider} which isn't directly connected to the network but one side is still providing info to the network these local providers are typically {@link IListReader}s. This is how the {@link IDisplay}s find connectable readers.
	 * @param tile the info provider
	 * @param face the face it is disconnecting from */
	public void onLocalProviderRemoved(IInfoProvider tile, EnumFacing face);

	/** can be called by a {@link INetworkTile}, only for when the part is altered after the {@link IDataCable} has been placed
	 * @param tile the network tile
	 * @param face the face it is connected to */
	public void onConnectionAdded(INetworkTile tile, EnumFacing face);

	/** can be called by a {@link INetworkTile}, only for when the part is altered after the {@link IDataCable} has been placed
	 * @param tile the network tile
	 * @param face the face it is disconnecting from */
	public void onConnectionRemoved(INetworkTile tile, EnumFacing face);

	/** adds all connections adjacent to the cable including...<pre>
	 *internal = (Multiparts which implement {@link INetworkTile} and are within the same block as the cable), 
	 *external = (Adjacent full blocks which implement {@link INetworkTile}),
	 *local providers = (see {@link IDataCable#onLocalProviderAdded})</pre>
	 * @param network the network to add the connections to */
	public void addConnections(ILogisticsNetwork network);

	/**removes all connection adjacent to the cable
	 * <p> see {@link IDataCable#addConnections}
	 * @param network the network to remove the connections from */
	public void removeConnections(ILogisticsNetwork network);

}
