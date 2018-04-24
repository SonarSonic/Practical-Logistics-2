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
        return EnumListChange.DECREASED;
    }
}
