package sonar.logistics.connections.monitoring;

import java.util.Objects;

import sonar.logistics.LogisticsASMLoader;
import sonar.logistics.api.info.IMonitorInfo;

public abstract class LogicMonitorHandler<I extends IMonitorInfo> {

	public abstract String id();

	public static LogicMonitorHandler instance(String id) {
		LogicMonitorHandler tileHandler = (LogicMonitorHandler) LogisticsASMLoader.tileMonitorHandlers.get(id);
		return tileHandler == null ? (LogicMonitorHandler) LogisticsASMLoader.entityMonitorHandlers.get(id) : tileHandler;
	}

	public int hashCode() {
		return Objects.hashCode(id());
	}

}
