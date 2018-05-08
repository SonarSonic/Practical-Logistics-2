package sonar.logistics.api.core.tiles.displays.info.lists;

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
