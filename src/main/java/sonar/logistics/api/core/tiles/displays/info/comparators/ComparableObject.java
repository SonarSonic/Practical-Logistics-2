package sonar.logistics.api.core.tiles.displays.info.comparators;

import sonar.logistics.api.core.tiles.displays.info.IInfo;

import javax.annotation.Nullable;
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

	@Nullable
	public static ComparableObject getComparableObject(List<ComparableObject> objs, String key) {
		for (ComparableObject obj : objs) {
			if (key.equals(obj.string)) {
				return obj;
			}
		}
		return null;

	}
}
