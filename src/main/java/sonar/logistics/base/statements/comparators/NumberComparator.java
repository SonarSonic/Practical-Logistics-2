package sonar.logistics.base.statements.comparators;

import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfoComparator;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicOperator;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicState;

import java.util.List;

@ASMInfoComparator(handlingClass = Number.class, id = NumberComparator.NAME, modid = PL2Constants.MODID)
public class NumberComparator implements ILogicComparator<Number> {

	public static final String NAME = "num";

	@Override
	public LogicState getLogicState(LogicOperator operator, Number info, Number object) {
		return LogicState.getState(operator.basicComparison(info.doubleValue(), object.doubleValue()));
	}

	@Override
	public List<LogicOperator> getValidOperators() {
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
		return NAME;
	}

}
