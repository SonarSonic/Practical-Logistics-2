package sonar.logistics.api.core.tiles.displays.info.lists;

public enum EnumListChange {

	INCREASED, // if the value has increased - if the value only changes and doesn't have an increased value this should still be used.

	DECREASED, // if the value has decreased

	EQUAL, // if no changes occurred to the value

	NEW_VALUE, // if the value hasn't been present before

	OLD_VALUE; // if the value is not longer present and should be removed

	public boolean shouldUpdate() {
		return this != EQUAL;
	}
	
	public boolean shouldDelete(){
		return this == OLD_VALUE;
	}

	public static EnumListChange getChange(long count, long old){
		if(old == 0 && count != 0){
			return EnumListChange.NEW_VALUE;
		}
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
