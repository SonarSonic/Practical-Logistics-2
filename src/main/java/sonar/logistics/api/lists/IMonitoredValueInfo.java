package sonar.logistics.api.lists;

public interface IMonitoredValueInfo<T> {

	IMonitoredValue<T> createMonitoredValue();
	
}
