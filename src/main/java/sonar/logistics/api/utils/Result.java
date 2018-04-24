package sonar.logistics.api.utils;


public enum Result {
	SUCCESS, PASS, FAIL;
	
	public boolean getBoolean(){
		return this != Result.FAIL;
	}
}