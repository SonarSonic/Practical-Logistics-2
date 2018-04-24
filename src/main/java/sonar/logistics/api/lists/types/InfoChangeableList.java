package sonar.logistics.api.lists.types;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.values.InfoMonitoredValue;

public class InfoChangeableList<I extends IInfo<I>> extends AbstractChangeableList<I> {

	public static <I extends IInfo<I>> InfoChangeableList<I> newChangeableList(){
		return new InfoChangeableList<>();
	}
	
	@Override
	public InfoMonitoredValue<I> createMonitoredValue(I obj) {
		return new InfoMonitoredValue<>(obj);
	}
}
