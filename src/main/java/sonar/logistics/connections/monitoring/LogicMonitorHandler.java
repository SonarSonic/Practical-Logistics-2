package sonar.logistics.connections.monitoring;

import java.util.Objects;

import sonar.logistics.PL2ASMLoader;
import sonar.logistics.api.info.IMonitorInfo;

public abstract class LogicMonitorHandler<I extends IMonitorInfo> {

	public abstract String id();

	public static LogicMonitorHandler instance(String id) {
		LogicMonitorHandler tileHandler = (LogicMonitorHandler) PL2ASMLoader.tileMonitorHandlers.get(id);
		return tileHandler == null ? (LogicMonitorHandler) PL2ASMLoader.entityMonitorHandlers.get(id) : tileHandler;
	}

	public int hashCode() {
		return Objects.hashCode(id());
	}

}
