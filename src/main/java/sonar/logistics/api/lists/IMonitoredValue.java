package sonar.logistics.api.lists;

public interface IMonitoredValue<T> {

	public void reset(T fullInfo);
	
	public EnumListChange getChange();
	
	public void resetChange();

	public void combine(T combine);
	
	public boolean isValid(Object info);

	public boolean canCombine(T combine);
	
	public void setNew();
	
	public boolean shouldDelete(EnumListChange change);
	
	public T getSaveableInfo();
}
