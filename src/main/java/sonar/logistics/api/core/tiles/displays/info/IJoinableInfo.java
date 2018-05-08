package sonar.logistics.api.core.tiles.displays.info;

/**implemented on info which can be joined in a list, 
 * used for things like Items or Fluids which are joined together to show a value across the whole system rather than individual stacks*/
public interface IJoinableInfo<T extends IInfo> extends IInfo<T> {

	/**can this info be joined together*/
    boolean canJoinInfo(T info);
	
	/**join the info*/
    IJoinableInfo joinInfo(T info);
}
