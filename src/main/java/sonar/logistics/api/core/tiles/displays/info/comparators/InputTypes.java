package sonar.logistics.api.core.tiles.displays.info.comparators;

import sonar.logistics.base.statements.comparators.BooleanComparator;
import sonar.logistics.base.statements.comparators.NumberComparator;
import sonar.logistics.base.statements.comparators.ObjectComparator;

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
