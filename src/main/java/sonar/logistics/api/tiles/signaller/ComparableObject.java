package sonar.logistics.api.tiles.signaller;

import sonar.logistics.api.info.IInfo;

import java.util.List;

public class ComparableObject {

	public IInfo source;
	public String string;
	public Object object;

	public ComparableObject(IInfo source, String string, Object object) {
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
