package sonar.logistics.api.info;

import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;

public interface IProvidableInfo<T extends IInfo> extends IInfo<T> {

	public RegistryType getRegistryType();
	
	public T setRegistryType(RegistryType type);
	
	public void setFromReturn(LogicPath path, Object returned);
}
