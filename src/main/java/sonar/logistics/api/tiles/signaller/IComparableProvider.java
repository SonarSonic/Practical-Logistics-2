package sonar.logistics.api.tiles.signaller;

import java.util.Map;

public interface IComparableProvider<T> {

	public void getComparableObjects(String parent, T obj, Map<LogicIdentifier, Object> objects);
	
}
