package sonar.logistics.core.tiles.displays.info.types.general;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.lists.EnumListChange;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;

public class InfoMonitoredValue<I extends IInfo<I>> implements IMonitoredValue<I> {

	public I cachedInfo;
	public EnumListChange currentState;

	public InfoMonitoredValue(I fullInfo) {
		reset(fullInfo);
		currentState = EnumListChange.NEW_VALUE;
	}

	@Override
	public void reset(I fullInfo) {
		cachedInfo = fullInfo.copy();
	}

	@Override
	public EnumListChange getChange() {
		return currentState;
	}

	@Override
	public void resetChange() {
		currentState = EnumListChange.OLD_VALUE;
	}

	@Override
	public void combine(I combine) {
		EnumListChange newState;
		if (!cachedInfo.isIdenticalInfo(combine)) {
			cachedInfo.identifyChanges(combine);// FIXME should we be doing this on a monitoredlist everytime?
			reset(combine);
			newState = EnumListChange.INCREASED;
		} else {
			newState = EnumListChange.EQUAL;
		}
		if (currentState != EnumListChange.NEW_VALUE) {
			currentState = newState;
		}
	}

	@Override
	public boolean isValid(Object info) {
		return info instanceof IInfo;
	}

	@Override
	public boolean canCombine(I combine) {
		return cachedInfo.isMatchingType(combine) && cachedInfo.isMatchingInfo(combine);
	}

	@Override
	public I getSaveableInfo() {
		return cachedInfo;
	}

	@Override
	public void setNew() {
		this.currentState = EnumListChange.NEW_VALUE;
	}

	@Override
	public boolean shouldDelete(EnumListChange change) {
		return change.shouldDelete();
	}

}
