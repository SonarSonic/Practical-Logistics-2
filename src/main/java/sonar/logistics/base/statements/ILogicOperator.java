package sonar.logistics.base.statements;

import sonar.logistics.api.core.tiles.displays.info.comparators.LogicState;

public interface ILogicOperator {

	LogicState basicComparison(int num1, int num2);
	
}
