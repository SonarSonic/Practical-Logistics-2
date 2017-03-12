package sonar.logistics.api.readers;

import sonar.logistics.api.info.IMonitorInfo;

public interface IReader<T extends IMonitorInfo> extends INetworkReader<T> {

}
