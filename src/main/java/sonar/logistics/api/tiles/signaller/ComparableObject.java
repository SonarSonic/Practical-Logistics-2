package sonar.logistics.api.tiles.signaller;

import java.util.List;

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

	public static ComparableObject getComparableObject(List<ComparableObject> objs, String key) {
		for (ComparableObject obj : objs) {
			if (key.equals(obj.string)) {
				return obj;
			}
		}
		return null;

	}
}
