package sonar.logistics.api.core.tiles.readers;

import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;

public interface ILogicListSorter<T> extends INBTSyncable {

	String getRegisteredName();
	
	boolean canSort(Object obj);

	AbstractChangeableList<T> sortSaveableList(AbstractChangeableList<T> updateInfo);
	
}
