package sonar.logistics.api.info.register;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.utils.MonitoredList;

public interface IMasterInfoRegistry {

	public void registerInfoRegistry(String modid, IInfoRegistry handler);

	/**registers a valid Capability which can be used to provide LogicInfo*/
	public void registerCapability(Capability capability);

	/**registers a valid Return Type which can be used to provide LogicInfo*/
	public void registerValidReturn(Class<?> classType);
	
	/**attempts to register all accessible methods in the given Class which can be used to provide LogicInfo, 
	 * only methods with Valid Returns will be registered, use the "registerReturn" method to make sure a specific return type is registered*/
	public void registerMethods(Class<?> classType, RegistryType type);

	/**attempts to register all accessible methods with matching method names to those provided
	 * only methods with Valid Returns will be registered, use the "registerReturn" method to make sure a specific return type is registered*/
	public void registerMethods(Class<?> classType, RegistryType type, List<String> methodNames);

	/**attempts to register all accessible methods with matching method names to those provided, or you can choose to exclude the names provided and register all others
	 * only methods with Valid Returns will be registered, use the "registerReturn" method to make sure a specific return type is registered*/
	public void registerMethods(Class<?> classType, RegistryType type, List<String> methodNames, boolean exclude);

	/**allows you to give field names the same translation as another
	 * @param fieldName the field which already offers the correct translation
	 * @param fieldNames the fields you wish to change the translation for*/
	public void registerClientNames(String fieldName, List<String> fieldNames);

	/**attempts to register all accessible fields in the given Class which can be used to provide LogicInfo, 
	 * only fields with Valid Objects will be registered, use the "registerReturn" method to make sure a specific object type is registered*/
	public void registerFields(Class<?> classType, RegistryType type);

	/**attempts to register all accessible fields with matching field names to those provided, 
	 * only fields with Valid Objects will be registered, use the "registerReturn" method to make sure a specific object type is registered*/
	public void registerFields(Class<?> classType, RegistryType type, List<String> fieldNames);

	/**attempts to register all accessible fields with matching fields names to those provided, or you can choose to exclude the names provided and register all others,
	 * only fields with Valid Objects will be registered, use the "registerReturn" method to make sure a specific object type is registered*/
	public void registerFields(Class<?> classType, RegistryType type, List<String> fieldNames, boolean exclude);

	/**used for registering IInventory fields which can supply LogicInfo*/
	public void registerInvFields(Class<?> inventoryClass, Map<String, Integer> fields);

	/**registers a specific prefix/suffix to the info with the given identifiers. Identifiers are formatted as "pl." + *Simple Class Name* + "." + method/field name  */
	public void registerInfoAdjustments(List<String> identifiers, String prefix, String suffix);

	/**registers a specific prefix/suffix to the info with the given identifier. Identifiers are formatted as "pl." + *Simple Class Name* + "." + method/field name  */
	public void registerInfoAdjustments(String identifier, String prefix, String suffix);

	public boolean containsAssignableType(Class<?> toCheck, List<Class<?>> classes);
	
	public boolean isValidReturnType(Class<?> returnType);
	
	public boolean validateParameters(Class<?>[] parameters);

	public List<Method> getAssignableMethods(Class<?> obj, RegistryType type);

	public List<Field> getAccessibleFields(Class<?> obj, RegistryType type);	

	public Object invokeMethod(Object obj, Method method, Object... available);
	
	public void getClassInfo(List<IProvidableInfo> infoList, LogicPath currentPath, RegistryType type, Object obj, Method method, Object... available);
	
	public void getFieldInfo(List<IProvidableInfo> infoList, LogicPath currentPath, RegistryType type, Object obj, Field field, Object... available);	

	public void buildInfo(List<IProvidableInfo> infoList, LogicPath path, String className, String fieldName, RegistryType type, Object object);	

	public Object getField(Object obj, Field field);	

	public List<IProvidableInfo> getEntityInfo(final List<IProvidableInfo> infoList, Entity entity);	

	public List<IProvidableInfo> getTileInfo(final List<IProvidableInfo> infoList, EnumFacing currentFace, Object... available);	

	public void addCapabilities(final List<IProvidableInfo> infoList, LogicPath path, Object obj, EnumFacing currentFace, Object... available);

	/**gets the very latest version of the LogicInfo provided from the MonitoredList provided*/
	public Pair<Boolean, IProvidableInfo> getLatestInfo(MonitoredList updateInfo, List<NodeConnection> connections, IInfo monitorInfo);

	/**gets the very latest version of the LogicInfo provided from the Node Connection provided*/
	public Pair<Boolean, IProvidableInfo> getLatestInfo(IProvidableInfo info, NodeConnection entry);

	/**gets the very latest info from the available objects using the correct LogicPath, this enables maximum efficiency, reducing the need for all methods to be called.*/
	public IProvidableInfo getInfoFromPath(IProvidableInfo info, LogicPath logicPath, EnumFacing currentFace, Object... available);
}
