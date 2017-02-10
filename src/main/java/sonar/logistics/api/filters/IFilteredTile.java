package sonar.logistics.api.filters;

import java.util.ArrayList;

import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.network.SyncFilterList;

public interface IFilteredTile extends ILogicViewable {

	public SyncFilterList getFilters();
	
}
