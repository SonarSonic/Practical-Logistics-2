package sonar.logistics.info.comparators;

import sonar.core.api.IRegistryObject;
import sonar.logistics.api.tiles.signaller.LogicOperator;
import sonar.logistics.api.tiles.signaller.LogicState;

import java.util.List;

public interface ILogicComparator<T> extends IRegistryObject {

	LogicState getLogicState(LogicOperator operator, T info, T object);

	List<LogicOperator> getValidOperators();
	
	boolean isValidObject(Object obj);

}
