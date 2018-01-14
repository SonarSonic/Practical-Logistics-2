package sonar.logistics.api.networks;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.tiles.nodes.EntityConnection;

/** implemented on {@link INetworkHandler}s which can provide info for Entities */
public interface IEntityMonitorHandler<I extends IInfo, L extends AbstractChangeableList, C extends INetworkChannels> extends INetworkHandler {

	/** updates info for the given entity.
	 * @param channels the {@link INetworkChannels} calling for the updated info
	 * @param newList the new list given by the {@link INetworkChannels}
	 * @param previousList the last list the handler gave for this connection
	 * @param entity the entity connection to provide info for
	 * @return the list to set the updated info to. */
	public L updateInfo(C channels, L newList, EntityConnection entity);
}
