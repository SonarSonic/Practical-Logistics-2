package sonar.logistics.api.tiles.signaller;

import sonar.logistics.info.comparators.BooleanComparator;
import sonar.logistics.info.comparators.NumberComparator;
import sonar.logistics.info.comparators.ObjectComparator;

public enum InputTypes {
	STRING(ObjectComparator.NAME), //
	BOOLEAN(BooleanComparator.NAME), //
	NUMBER(NumberComparator.NAME), //
	INFO(ObjectComparator.NAME); //

	public String comparatorID;

	InputTypes(String comparatorID) {
		this.comparatorID = comparatorID;
	}

	public boolean usesInfo() {
		return this == INFO;
	}
}
