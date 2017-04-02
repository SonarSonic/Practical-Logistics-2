package sonar.logistics.api.info;

public abstract class IInfoRegistry {

	/**register all the possible returns of the base methods which are not primitive types and register any capabilities
	 * @param registry the registry*/
	public void registerBaseReturns(ILogicInfoRegistry registry){}

	/**register all the possible base methods including the ones of base returns to ensure only primitive types are returns
	 * @param registry the registry*/
	public void registerBaseMethods(ILogicInfoRegistry registry){}
	
	/**register any IInventory fields which can be read here
	 * @param registry the registry*/
	public void registerAllFields(ILogicInfoRegistry registry){}
	
	/**register any prefixes or suffixes to be used on any type of returns
	 * @param registry the registry*/
	public void registerAdjustments(ILogicInfoRegistry registry){}
	
}
