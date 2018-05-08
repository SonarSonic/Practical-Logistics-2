package sonar.logistics.base.filters;

import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.logistics.base.tiles.IChannelledTile;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.network.sync.SyncFilterList;

public interface IFilteredTile extends IChannelledTile, ILogicListenable, IInventoryFilter {

	SyncFilterList getFilters();
	
	int getSlotID();
	
}
