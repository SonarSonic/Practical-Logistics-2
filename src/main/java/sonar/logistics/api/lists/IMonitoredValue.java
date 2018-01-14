package sonar.logistics.api.lists;

import sonar.logistics.api.info.IInfo;

public interface IMonitoredValue<T> {

	public void reset(T fullInfo);
	
	public EnumListChange getChange();
	
	public void resetChange();

	public void combine(T combine);
	
	public boolean isValid(Object info);

	public boolean canCombine(T combine);
	
	public T getSaveableInfo();
}
