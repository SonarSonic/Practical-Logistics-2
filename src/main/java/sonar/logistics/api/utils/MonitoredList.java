package sonar.logistics.api.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import sonar.core.api.StorageSize;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.IJoinableInfo;

public class MonitoredList<T extends IInfo> extends ArrayList<T> {

	public List<T> changed = new ArrayList<T>();
	public List<T> removed = new ArrayList<T>();
	public StorageSize sizing;
	public boolean hasChanged = true;
	public int networkID;// embedded network ID to wipe lists from the wrong network on client side, avoiding empty lists being sent and preventing monitors displaying old data

	public MonitoredList(int networkID) {
		super();
		this.networkID = networkID;
	}

	public MonitoredList(int networkID, List<T> items, StorageSize sizing, List<T> changed, List<T> removed) {
		super(items);
		this.networkID = networkID;
		this.sizing = sizing;
		this.changed = changed;
		this.removed = removed;
	}

	public static <I extends IInfo> MonitoredList<I> newMonitoredList(int networkID) {
		return new MonitoredList<I>(networkID, Lists.<I>newArrayList(), new StorageSize(0, 0), Lists.<I>newArrayList(), Lists.<I>newArrayList());
	}

	public List<T> cloneInfo() {
		return (List<T>) super.clone();
	}

	public MonitoredList<T> setInfo(List<T> list) {
		clear();
		addAll(list);
		return this;
	}

	public MonitoredList<T> copyInfo() {
		return new MonitoredList<T>(networkID, (List<T>) cloneInfo(), new StorageSize(sizing.getStored(), sizing.getMaxStored()), Lists.newArrayList(changed), Lists.newArrayList(removed));
	}

	public T findInfoInList(T newInfo, MonitoredList<T> previousList) {
		for (T lastInfo : previousList) {
			if (lastInfo.isIdenticalInfo(newInfo)) {
				return lastInfo;
			}
		}
		return null;
	}

	public void addInfoToList(T newInfo, MonitoredList<T> previousList) {
		if (newInfo instanceof IJoinableInfo) {
			for (int i = 0; i < this.size(); i++) {
				T storedInfo = this.get(i);
				if (((IJoinableInfo) storedInfo).canJoinInfo(newInfo)) {
					set(i, (T) ((IJoinableInfo) storedInfo).joinInfo(newInfo)); // should I copy the new info???? if stuff gets messed up copy it
					return;
				}
			}
		}
		add(newInfo);
	}

	public MonitoredList<T> updateList(MonitoredList<?> monitoredList) {
		List<T> changed = ((List<T>) cloneInfo());
		List<T> removed = ((List<T>) monitoredList.cloneInfo());

		((List<T>) monitoredList.cloneInfo()).forEach(last -> forEach(current -> {
			if (last.isMatchingType(current)) {
				if (last.isMatchingInfo(current)) {
					removed.remove(last);
				}
				if (last.isIdenticalInfo(current)) {
					changed.remove(current);
				}
			}
		}));
		this.changed = changed;
		this.removed = removed;
		hasChanged = !changed.isEmpty() || !removed.isEmpty();
		return this;
	}

	public void markDirty() {
		hasChanged = true;
	}

	/** @param info the info type you wish to check
	 * @return a boolean for if the info was changed and the new info */
	public Pair<Boolean, IInfo> getLatestInfo(IInfo oldInfo) {
		for (T newInfo : this) {
			if (newInfo.isMatchingType(oldInfo) && newInfo.isMatchingInfo(oldInfo)) {
				return new Pair(!newInfo.isIdenticalInfo(oldInfo), newInfo);
			}
		}
		return new Pair(false, oldInfo);
	}
}