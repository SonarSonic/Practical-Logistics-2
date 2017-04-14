package sonar.logistics.api.info;

/**implemented on info which can be rendered in a list in the Info Reader*/
public interface INameableInfo<T extends IInfo> extends IInfo<T> {
	
	/**the objects identifier (translated)*/
	public String getClientIdentifier();
	
	/**the object itself*/
	public String getClientObject();

	/**the object type*/
	public String getClientType();
}
