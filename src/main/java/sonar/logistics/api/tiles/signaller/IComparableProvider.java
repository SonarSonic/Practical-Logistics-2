package sonar.logistics.api.tiles.signaller;

import java.util.Map;

public interface IComparableProvider<T> {

	void getComparableObjects(String parent, T obj, Map<LogicIdentifier, Object> objects);
	
}
