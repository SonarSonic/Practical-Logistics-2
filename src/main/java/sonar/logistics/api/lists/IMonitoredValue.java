package sonar.logistics.api.lists;

public interface IMonitoredValue<T> {

	void reset(T fullInfo);
	
	EnumListChange getChange();
	
	void resetChange();

	void combine(T combine);
	
	boolean isValid(Object info);

	boolean canCombine(T combine);
	
	void setNew();
	
	boolean shouldDelete(EnumListChange change);
	
	T getSaveableInfo();
}
