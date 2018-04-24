package sonar.logistics.api.tiles.readers;

import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.lists.types.AbstractChangeableList;

public interface ILogicListSorter<T> extends INBTSyncable {

	String getRegisteredName();
	
	boolean canSort(Object obj);

	AbstractChangeableList<T> sortSaveableList(AbstractChangeableList<T> updateInfo);
	
}
