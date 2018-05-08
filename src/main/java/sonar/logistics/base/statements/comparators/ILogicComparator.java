package sonar.logistics.base.statements.comparators;

import sonar.core.api.IRegistryObject;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicOperator;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicState;

import java.util.List;

public interface ILogicComparator<T> extends IRegistryObject {

	LogicState getLogicState(LogicOperator operator, T info, T object);

	List<LogicOperator> getValidOperators();
	
	boolean isValidObject(Object obj);

}
