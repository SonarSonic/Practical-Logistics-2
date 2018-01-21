package sonar.logistics.logic.comparators;

import java.util.List;

import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicComparator;
import sonar.logistics.api.tiles.signaller.InputTypes;
import sonar.logistics.api.tiles.signaller.LogicOperator;
import sonar.logistics.api.tiles.signaller.LogicState;

@LogicComparator(handlingClass = Boolean.class, id = BooleanComparator.NAME, modid = PL2Constants.MODID)
public class BooleanComparator implements ILogicComparator<Boolean> {

	public static final String NAME = "bool";

	@Override
	public LogicState getLogicState(LogicOperator operator, Boolean info, Boolean object) {
		boolean bool = info.booleanValue() == object.booleanValue();
		return operator == LogicOperator.EQUALS ? LogicState.getState(bool) : LogicState.getState(!bool);
	}

	@Override
	public List<LogicOperator> getValidOperators() {
		return LogicOperator.switchOperators;
	}

	@Override
	public boolean isValidObject(Object obj) {
		return obj instanceof Boolean;
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
