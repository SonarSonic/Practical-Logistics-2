package sonar.logistics.api.lists;

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

}
