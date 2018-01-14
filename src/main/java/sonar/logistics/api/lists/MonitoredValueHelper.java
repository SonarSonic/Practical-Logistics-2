package sonar.logistics.api.lists;

public class MonitoredValueHelper {
	
	public static EnumListChange getChange(long count, long old){
		if (count == 0) {
			return EnumListChange.OLD_VALUE;
		}
		if (count == old) {
			return EnumListChange.EQUAL;
		}
		if (count > old) {
			return EnumListChange.INCREASED;
		}
		if (count < old) {
			return EnumListChange.DECREASED;
		}
		return EnumListChange.NEW_VALUE;
	}
}
