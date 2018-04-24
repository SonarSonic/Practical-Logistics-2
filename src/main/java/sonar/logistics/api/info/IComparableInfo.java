package sonar.logistics.api.info;

import java.util.List;

import sonar.logistics.api.tiles.signaller.ComparableObject;

public interface IComparableInfo<T extends IInfo> extends IInfo<T> {

	List<ComparableObject> getComparableObjects(List<ComparableObject> objects);

}
