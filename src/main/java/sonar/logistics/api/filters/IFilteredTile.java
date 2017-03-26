package sonar.logistics.api.filters;

import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.network.sync.SyncFilterList;

public interface IFilteredTile extends IChannelledTile, ILogicViewable, IInventoryFilter {

	public SyncFilterList getFilters();
}
