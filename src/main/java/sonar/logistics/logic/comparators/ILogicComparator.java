package sonar.logistics.logic.comparators;

import java.util.ArrayList;
import java.util.List;

import sonar.core.api.IRegistryObject;
import sonar.logistics.api.logistics.LogicOperator;
import sonar.logistics.api.logistics.LogicState;

public interface ILogicComparator<T> extends IRegistryObject {

	public LogicState getLogicState(LogicOperator operator, T info, T object);

	public ArrayList<LogicOperator> getValidOperators();
	
	public boolean isValidObject(Object obj);

}
