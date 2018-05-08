package sonar.logistics.base.statements.comparators;

import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfoComparator;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicOperator;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicState;

import java.util.List;

@ASMInfoComparator(handlingClass = Object.class, id = ObjectComparator.NAME, modid = PL2Constants.MODID)
public class ObjectComparator implements ILogicComparator<Object> {

	public static final String NAME = "obj";

	@Override
	public LogicState getLogicState(LogicOperator operator, Object info, Object object) {
		switch (operator) {
		case NOT_EQUALS:
			return LogicState.getState(!info.equals(object));
		case EQUALS:
			return LogicState.getState(info.equals(object));
		default:
			return LogicState.FALSE;
		}
	}

	@Override
	public List<LogicOperator> getValidOperators() {
		return LogicOperator.switchOperators;
	}

	@Override
	public boolean isValidObject(Object obj) {
		return true;
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