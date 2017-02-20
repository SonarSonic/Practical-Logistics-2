package sonar.logistics.api.logistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;

public interface ILogisticsStatement {

	/** gets the operator of the statement */
	public LogicOperator getOperator();

	/** checks the statement is matching */
	public LogicState isMatching(Map<InfoUUID, IMonitorInfo> info);

	/** get valid operators */
	public ArrayList<LogicOperator> validOperators();
}
