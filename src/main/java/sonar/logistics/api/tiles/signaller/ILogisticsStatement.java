package sonar.logistics.api.tiles.signaller;

import java.util.List;
import java.util.Map;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;

public interface ILogisticsStatement {

	/** gets the operator of the statement */
	public LogicOperator getOperator();

	/** checks the statement is matching */
	public LogicState isMatching(Map<InfoUUID, IInfo> info);

	/** get valid operators */
	public List<LogicOperator> validOperators();
}
