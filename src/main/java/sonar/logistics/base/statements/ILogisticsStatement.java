package sonar.logistics.base.statements;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicOperator;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicState;

import java.util.List;
import java.util.Map;

public interface ILogisticsStatement {

	/** gets the operator of the statement */
    LogicOperator getOperator();

	/** checks the statement is matching */
    LogicState isMatching(Map<InfoUUID, IInfo> info);

	/** get valid operators */
    List<LogicOperator> validOperators();
}
