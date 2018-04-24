package sonar.logistics.api.lists.types;

import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.IMonitoredValueInfo;

/**can hold all types of info*/
public class UniversalChangeableList<T> extends AbstractChangeableList<IMonitoredValueInfo<T>> {

	public static <T> UniversalChangeableList<T> newChangeableList(){
		return new UniversalChangeableList<>();
	}
	
	@Override
	public IMonitoredValue createMonitoredValue(IMonitoredValueInfo<T> obj) {
		return obj.createMonitoredValue();
	}

}
