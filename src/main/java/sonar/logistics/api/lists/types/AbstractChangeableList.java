package sonar.logistics.api.lists.types;

import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.tiles.readers.ILogicListSorter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractChangeableList<T> {

	public List<IMonitoredValue<T>> values = new ArrayList<>();
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
		List<IMonitoredValue<T>> toDelete = new ArrayList<>();
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
		List<T> saveable = new ArrayList<>();
		values.forEach(value -> saveable.add(value.getSaveableInfo()));
		return saveable;
	}

	public List<T> createSaveableList(@Nullable ILogicListSorter sorter) {
		if(sorter!=null && !values.isEmpty()){
			if(sorter.canSort(values.get(0))){
				sorter.sortSaveableList(this);
			}
		}
		return createSaveableList();
	}

	public abstract IMonitoredValue<T> createMonitoredValue(T obj);
}
