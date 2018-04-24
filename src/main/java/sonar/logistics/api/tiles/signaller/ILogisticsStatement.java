package sonar.logistics.api.tiles.signaller;

import java.util.List;
import java.util.Map;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;

public interface ILogisticsStatement {

	/** gets the operator of the statement */
    LogicOperator getOperator();

	/** checks the statement is matching */
    LogicState isMatching(Map<InfoUUID, IInfo> info);

	/** get valid operators */
    List<LogicOperator> validOperators();
}
