package sonar.logistics.api.info;

import java.util.List;

import sonar.logistics.api.tiles.signaller.ComparableObject;

public interface IComparableInfo<T extends IMonitorInfo> extends IMonitorInfo<T> {

	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects);

}
