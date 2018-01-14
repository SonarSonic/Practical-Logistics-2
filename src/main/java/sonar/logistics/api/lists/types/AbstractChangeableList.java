package sonar.logistics.api.lists.types;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import sonar.core.api.inventories.StoredItemStack;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.IMonitoredValue;

public abstract class AbstractChangeableList<T> {

	public static final Consumer<? super IMonitoredValue> saveState = IMonitoredValue::resetChange;
	public List<IMonitoredValue<T>> list = Lists.newArrayList();
	public boolean wasLastListNull = false;

	public AbstractChangeableList() {}

	public List<IMonitoredValue<T>> getList() {
		return list;
	}

	public int getValueCount() {
		return list.size();
	}

	public T getActualValue(int i) {
		if (i < getValueCount()) {
			return list.get(i).getSaveableInfo();
		}
		return null;
	}

	public void add(T obj) {
		IMonitoredValue<T> found = find(obj);
		if (found == null) {
			list.add(createMonitoredValue(obj));
		} else {
			doCombine(found, obj);
		}
	}

	@Nullable
	public IMonitoredValue<T> find(T obj) {
		for (IMonitoredValue<T> value : list) {
			if (value.canCombine(obj)) {
				return value;
			}
		}
		return null;
	}

	public void saveStates() {
		List<IMonitoredValue<T>> toDelete = Lists.newArrayList();
		list.forEach(value -> {
			if (value.getChange().shouldDelete()) {
				toDelete.add(value);
			} else {
				value.resetChange();
			}
		});
		toDelete.forEach(value -> list.remove(value));
		wasLastListNull = list.isEmpty();
	}

	public void doCombine(IMonitoredValue<T> value, T obj) {
		value.combine(obj);
	}

	public List<T> createSaveableList() {
		List<T> values = Lists.newArrayList();
		list.forEach(value -> values.add(value.getSaveableInfo()));
		return values;
	}

	public abstract IMonitoredValue<T> createMonitoredValue(T obj);
}
