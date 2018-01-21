package sonar.logistics.api.tiles.signaller;

import sonar.logistics.logic.comparators.BooleanComparator;
import sonar.logistics.logic.comparators.NumberComparator;
import sonar.logistics.logic.comparators.ObjectComparator;

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
