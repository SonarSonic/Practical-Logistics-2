package sonar.logistics.api.core.tiles.displays.info;

import sonar.logistics.api.core.tiles.displays.info.comparators.ComparableObject;

import java.util.List;

public interface IComparableInfo<T extends IInfo> extends IInfo<T> {

	List<ComparableObject> getComparableObjects(List<ComparableObject> objects);

}
