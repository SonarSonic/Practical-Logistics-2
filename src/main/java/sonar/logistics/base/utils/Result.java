package sonar.logistics.base.utils;


public enum Result {
	SUCCESS, PASS, FAIL;
	
	public boolean getBoolean(){
		return this != Result.FAIL;
	}
}