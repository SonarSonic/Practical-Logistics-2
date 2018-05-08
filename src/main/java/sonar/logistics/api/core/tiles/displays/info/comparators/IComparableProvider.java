package sonar.logistics.api.core.tiles.displays.info.comparators;

import java.util.Map;

public interface IComparableProvider<T> {

	void getComparableObjects(String parent, T obj, Map<LogicIdentifier, Object> objects);
	
}
