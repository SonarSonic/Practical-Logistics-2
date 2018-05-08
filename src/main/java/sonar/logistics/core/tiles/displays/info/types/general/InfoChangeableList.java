package sonar.logistics.core.tiles.displays.info.types.general;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;

public class InfoChangeableList<I extends IInfo<I>> extends AbstractChangeableList<I> {

	public static <I extends IInfo<I>> InfoChangeableList<I> newChangeableList(){
		return new InfoChangeableList<>();
	}
	
	@Override
	public InfoMonitoredValue<I> createMonitoredValue(I obj) {
		return new InfoMonitoredValue<>(obj);
	}
}
