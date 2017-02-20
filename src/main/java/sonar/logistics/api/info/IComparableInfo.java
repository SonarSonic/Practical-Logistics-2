package sonar.logistics.api.info;

import java.util.ArrayList;

import sonar.logistics.api.logistics.ComparableObject;

public interface IComparableInfo<T extends IMonitorInfo> extends IMonitorInfo<T> {

	public ArrayList<ComparableObject> getComparableObjects(ArrayList<ComparableObject> objects);

}
