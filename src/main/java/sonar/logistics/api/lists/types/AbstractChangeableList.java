package sonar.logistics.api.lists.types;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import sonar.logistics.api.lists.IMonitoredValue;

public abstract class AbstractChangeableList<T> {

	public static final Consumer<? super IMonitoredValue> saveState = IMonitoredValue::resetChange;
	public List<IMonitoredValue<T>> values = Lists.newArrayList();
	public boolean wasLastListNull = false;

	public AbstractChangeableList() {}

	public List<IMonitoredValue<T>> getList() {
		return values;
	}

	public int getValueCount() {
		return values.size();
	}

	public T getActualValue(int i) {
		if (i < getValueCount()) {
			return values.get(i).getSaveableInfo();
		}
		return null;
	}

	public void add(T obj) {
		IMonitoredValue<T> found = find(obj);
		if (found == null) {
			values.add(createMonitoredValue(obj));
		} else {
			doCombine(found, obj);
		}
	}

	@Nullable
	public IMonitoredValue<T> find(T obj) {
		for (IMonitoredValue<T> value : values) {
			if (value.canCombine(obj)) {
				return value;
			}
		}
		return null;
	}

	public void saveStates() {
		List<IMonitoredValue<T>> toDelete = Lists.newArrayList();
		values.forEach(value -> {
			if (value.getChange().shouldDelete()) {
				toDelete.add(value);
			} else {
				value.resetChange();
			}
		});
		toDelete.forEach(value -> values.remove(value));
		wasLastListNull = values.isEmpty();
	}

	public void doCombine(IMonitoredValue<T> value, T obj) {
		value.combine(obj);
	}

	public List<T> createSaveableList() {
		List<T> saveable = Lists.newArrayList();
		values.forEach(value -> saveable.add(value.getSaveableInfo()));
		return saveable;
	}

	public abstract IMonitoredValue<T> createMonitoredValue(T obj);
}
