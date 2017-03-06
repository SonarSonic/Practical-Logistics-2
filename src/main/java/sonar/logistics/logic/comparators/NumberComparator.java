package sonar.logistics.logic.comparators;

import java.util.ArrayList;

import sonar.logistics.api.asm.LogicComparator;
import sonar.logistics.api.logistics.LogicOperator;
import sonar.logistics.api.logistics.LogicState;

@LogicComparator(handlingClass = Number.class)
public class NumberComparator implements ILogicComparator<Number> {

	@Override
	public LogicState getLogicState(LogicOperator operator, Number info, Number object) {
		return LogicState.getState(operator.basicComparison(info.doubleValue(), object.doubleValue()));
	}

	@Override
	public ArrayList<LogicOperator> getValidOperators() {
		return LogicOperator.numOperators;
	}

	@Override
	public boolean isValidObject(Object obj) {
		return obj instanceof Number;
	}

	@Override
	public boolean isLoadable() {
		return true;
	}

	@Override
	public String getName() {
		return "num";
	}

}
