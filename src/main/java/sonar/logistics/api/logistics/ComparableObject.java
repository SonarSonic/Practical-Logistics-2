package sonar.logistics.api.logistics;

import java.util.ArrayList;

import sonar.logistics.api.info.IMonitorInfo;

public class ComparableObject {

	public IMonitorInfo source;
	public String string;
	public Object object;

	public ComparableObject(IMonitorInfo source, String string, Object object) {
		this.source = source;
		this.string = string;
		this.object = object;
	}

	public static ComparableObject getComparableObject(ArrayList<ComparableObject> objs, String key) {
		for (ComparableObject obj : objs) {
			if (key.equals(obj.string)) {
				return obj;
			}
		}
		return null;

	}
}
