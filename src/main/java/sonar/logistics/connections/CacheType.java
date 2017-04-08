package sonar.logistics.connections;

public enum CacheType {
	/**all objects both local and global*/
	ALL,
	/**local objects only*/
	LOCAL, 

	/**VERY RARELY USED, you probably want ALL. this gives global objects only*/
	GLOBAL;
	
	public boolean isLocal(){
		return this==ALL || this==LOCAL;
	}
	
	public boolean isGlobal(){
		return this==ALL || this==GLOBAL;
	}
}
