package sonar.logistics.api.core.tiles.displays.info;

import sonar.logistics.api.core.tiles.displays.info.register.LogicPath;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;

public interface IProvidableInfo<T extends IInfo> extends IInfo<T> {

	RegistryType getRegistryType();
	
	T setRegistryType(RegistryType type);
	
	void setFromReturn(LogicPath path, Object returned);
}
