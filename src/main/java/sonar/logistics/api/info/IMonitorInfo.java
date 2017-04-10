package sonar.logistics.api.info;

import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.register.LogicPath;

/**for your info to be registered you must use {@link LogicInfoType} implement this for all types of info*/
public interface IMonitorInfo<T extends IMonitorInfo> extends INBTSyncable{
	
	/**this must be the same as the ID specified in {@link LogicInfoType}*/
	public String getID();
	
	/** if they are identical **/
	public boolean isIdenticalInfo(T info);

	/** if they are of the same type with just different values **/
	public boolean isMatchingInfo(T info);

	/** if they are of the same type with just different values **/
	public boolean isMatchingType(IMonitorInfo info);

	public boolean isHeader();
	
	public INetworkHandler getHandler();
	
	public boolean isValid();
	
	public LogicPath getPath();

	public T setPath(LogicPath path);
	
	/**it is essential that you copy the LogicPath also*/
	public T copy();
	
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos);
	
	public void renderSizeChanged(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos);
	
	public void identifyChanges(T newInfo);
	
}
