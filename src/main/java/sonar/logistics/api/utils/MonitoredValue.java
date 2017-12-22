package sonar.logistics.api.utils;

import sonar.core.helpers.NBTHelper.SyncType;

public class MonitoredValue {

	long current = 0;
	long last = 0;
	DirtyValue value = DirtyValue.UNCHANGED;

	public MonitoredValue(long set) {
		this.current = set;
	}

	public long getCurrent() {
		return current;
	}

	public long getLast() {
		return last;
	}

	public DirtyValue getValue() {
		return value;
	}

	public boolean shouldSave(SyncType type) {
		if (type.isType(SyncType.SAVE)) {
			return true;
		}
		if (type.isType(SyncType.DEFAULT_SYNC)) {
			return value != DirtyValue.UNCHANGED;
		}
		return false;
	}

	public void add(long add) {
		current += add;
	}

	public void remove(long remove) {
		current -= remove;
		if (current < 0) {
			current = 0;
		}
	}

	public void reset() {
		current = 0;
		last = 0;
		value = DirtyValue.UNCHANGED;
	}

	public void finalise() {
		if (current == 0) {
			value = DirtyValue.REMOVED;
		} else if (current == last) {
			value = DirtyValue.UNCHANGED;
		} else {
			value = current > last ? DirtyValue.INCREASED : DirtyValue.DECREASED;
		}
		last = current;
		current = 0;
	}
}