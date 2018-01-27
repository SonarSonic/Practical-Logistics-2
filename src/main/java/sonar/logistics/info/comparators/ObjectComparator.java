package sonar.logistics.info.comparators;

import java.util.List;

import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicComparator;
import sonar.logistics.api.tiles.signaller.LogicOperator;
import sonar.logistics.api.tiles.signaller.LogicState;

@LogicComparator(handlingClass = Object.class, id = ObjectComparator.NAME, modid = PL2Constants.MODID)
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