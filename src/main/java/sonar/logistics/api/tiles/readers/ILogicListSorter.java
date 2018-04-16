package sonar.logistics.api.tiles.readers;

import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.info.types.MonitoredItemStack;

public interface ILogicListSorter<T> extends INBTSyncable {

	String getRegisteredName();
	
	boolean canSort(Object obj);

	AbstractChangeableList<T> sortSaveableList(AbstractChangeableList<T> updateInfo);
	
}
