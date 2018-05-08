package sonar.logistics.api.core.tiles.readers.channels;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.base.channels.EntityConnection;

/** implemented on {@link INetworkHandler}s which can provide info for Entities */
public interface IEntityMonitorHandler<I extends IInfo, L extends AbstractChangeableList, C extends INetworkChannels> extends INetworkHandler {

	/** updates info for the given entity.
	 * @param channels the {@link INetworkChannels} calling for the updated info
	 * @param newList the new list given by the {@link INetworkChannels}
	 * @param entity the entity connection to provide info for
	 * @return the list to set the updated info to. */
    L updateInfo(C channels, L newList, EntityConnection entity);
}
