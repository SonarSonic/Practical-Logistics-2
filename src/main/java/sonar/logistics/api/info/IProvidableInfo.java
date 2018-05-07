package sonar.logistics.api.info;

import sonar.logistics.api.info.register.LogicPath;
import sonar.logistics.api.info.register.RegistryType;

public interface IProvidableInfo<T extends IInfo> extends IInfo<T> {

	RegistryType getRegistryType();
	
	T setRegistryType(RegistryType type);
	
	void setFromReturn(LogicPath path, Object returned);
}
