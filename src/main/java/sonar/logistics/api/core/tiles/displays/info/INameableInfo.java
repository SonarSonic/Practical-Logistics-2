package sonar.logistics.api.core.tiles.displays.info;

/**implemented on info which can be rendered in a list in the Info Reader*/
public interface INameableInfo<T extends IInfo> extends IInfo<T> {
	
	/**the objects identifier (translated)*/
    String getClientIdentifier();
	
	/**the object itself*/
    String getClientObject();

	/**the object type*/
    String getClientType();
}
