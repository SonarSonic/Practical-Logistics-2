package sonar.logistics.api.info;

import sonar.logistics.api.tiles.signaller.ComparableObject;

import java.util.List;

public interface IComparableInfo<T extends IInfo> extends IInfo<T> {

	List<ComparableObject> getComparableObjects(List<ComparableObject> objects);

}
