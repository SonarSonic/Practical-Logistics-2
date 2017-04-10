package sonar.logistics.api.utils;

public enum CacheType {
	/**all objects both local and global*/
	ALL,
	/**local objects only*/
	LOCAL, 
	/**VERY RARELY USED, you probably want {@link CacheType#ALL}. this gives global objects only*/
	GLOBAL;
	
	public boolean isLocal(){
		return this==ALL || this==LOCAL;
	}
	
	public boolean isGlobal(){
		return this==ALL || this==GLOBAL;
	}
}
