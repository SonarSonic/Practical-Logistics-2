package sonar.logistics.api.core.tiles.displays.info.register;

public interface IInfoRegistry {

	/**register all the possible returns of the base methods which are not primitive types and register any capabilities
	 * @param registry the registry*/
	default void registerBaseReturns(IMasterInfoRegistry registry){}

	/**register all the possible base methods including the ones of base returns to ensure only primitive types are returns
	 * @param registry the registry*/
	default void registerBaseMethods(IMasterInfoRegistry registry){}
	
	/**register any IInventory fields which can be read here
	 * @param registry the registry*/
	default void registerAllFields(IMasterInfoRegistry registry){}
	
	/**register any prefixes or suffixes to be used on any type of returns
	 * @param registry the registry*/
	default void registerAdjustments(IMasterInfoRegistry registry){}
	
}
