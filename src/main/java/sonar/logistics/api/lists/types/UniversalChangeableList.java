package sonar.logistics.api.lists.types;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import sonar.core.api.inventories.StoredItemStack;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.IMonitoredValueInfo;

/**can hold all types of info*/
public class UniversalChangeableList<T> extends AbstractChangeableList<IMonitoredValueInfo<T>> {

	public static final <T> UniversalChangeableList<T> newChangeableList(){
		return new UniversalChangeableList<T>();		
	}
	
	@Override
	public IMonitoredValue createMonitoredValue(IMonitoredValueInfo<T> obj) {
		return obj.createMonitoredValue();
	}

}
