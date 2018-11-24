package sonar.logistics.core.tiles.displays.info;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Loader;
import sonar.core.network.sync.ObjectType;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.handlers.IEntityInfoProvider;
import sonar.logistics.api.core.tiles.displays.info.handlers.ITileInfoProvider;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.register.IInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.IMasterInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.LogicPath;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.data.generators.items.ITileInventoryProvider;
import sonar.logistics.core.tiles.displays.info.paths.CapabilityMethod;
import sonar.logistics.core.tiles.displays.info.paths.InventoryField;
import sonar.logistics.core.tiles.displays.info.paths.TileHandlerMethod;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;
import sonar.logistics.core.tiles.displays.info.types.general.LogicInfo;
import sonar.logistics.core.tiles.readers.info.handling.InfoNetworkHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** where all the registering for LogicInfo happens */
public class MasterInfoRegistry implements IMasterInfoRegistry {

	public static MasterInfoRegistry INSTANCE = new MasterInfoRegistry();

	/** the cache of methods/fields applicable to a given tile. */
	public Map<Class<?>, List<Method>> cachedMethods = new LinkedHashMap<>();
	public Map<Class<?>, List<Field>> cachedFields = new LinkedHashMap<>();

	/** all the registries which can provide valid returns, methods and fields */
	public List<IInfoRegistry> infoRegistries = new ArrayList<>();

	/** all custom handlers which can provide custom info on blocks for tricky situations */
	public List<ITileInfoProvider> tileProviders = new ArrayList<>();
	public List<IEntityInfoProvider> entityProviders = new ArrayList<>();
	public List<ITileInventoryProvider> inventoryProviders = new ArrayList<>();

	/** all the register validated returns, methods and fields from the registries */
	public List<Class<?>> validReturns = new ArrayList<>();
	public List<Capability> validCapabilities = new ArrayList<>();
	public Map<RegistryType, Map<Class<?>, List<Method>>> validMethods = new LinkedHashMap<>();
	public Map<RegistryType, Map<Class<?>, List<Field>>> validFields = new LinkedHashMap<>();
	public Map<Class<?>, Map<String, Integer>> validInvFields = new LinkedHashMap<>();
	public Map<String, Pair<String, String>> infoAdjustments = new LinkedHashMap<>();
	public Map<String, String> clientAdjustments = new LinkedHashMap<>(); // to give other methods the lang id of others

	/** the default accepted returns */
	public List<Class<?>> acceptedReturns = RegistryType.buildList();
	public List<Class<?>> defaultReturns = Lists.newArrayList(String.class);

	public void init() {

		infoRegistries.forEach(registry -> {
			try {
				registry.registerBaseReturns(this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
		infoRegistries.forEach(registry -> {
			try {
				registry.registerBaseMethods(this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
		infoRegistries.forEach(registry -> {
			try {
				registry.registerAllFields(this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
		infoRegistries.forEach(registry -> {
			try {
				registry.registerAdjustments(this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	public void reload() {
		validReturns.clear();
		validMethods.clear();
		validFields.clear();
		validInvFields.clear();
		infoAdjustments.clear();
		clientAdjustments.clear();

		init();

		cachedFields.clear();
		cachedMethods.clear();
	}

	public void registerInfoRegistry(String modid, IInfoRegistry handler) {
		if (Loader.isModLoaded(modid)) {
			infoRegistries.add(handler);
		}
	}

	public void registerCapability(Capability capability) {
		validCapabilities.add(capability);
	}

	public void registerValidReturn(Class<?> classType) {
		validReturns.add(classType);
	}

	public void registerMethods(Class<?> classType, RegistryType type) {
		registerMethods(classType, type, new ArrayList<>(), true);
	}

	public void registerMethods(Class<?> classType, RegistryType type, List<String> methodNames) {
		registerMethods(classType, type, methodNames, false);
	}

	public void registerMethods(Class<?> classType, RegistryType type, List<String> methodNames, boolean exclude) {
		validMethods.putIfAbsent(type, new LinkedHashMap<>());
		validMethods.get(type).putIfAbsent(classType, new ArrayList<>());
		List<String> used = new ArrayList<>();
		Method[] methods = classType.getMethods();
		for (Method method : methods) {
			if (!used.contains(method.getName()) && (methodNames.isEmpty() || (exclude != methodNames.contains(method.getName())))) {
				boolean validParams = validateParameters(method.getParameterTypes()), validReturns = isValidReturnType(method.getReturnType());
				if (validParams && validReturns) {
					validMethods.get(type).get(classType).add(method);
					used.add(method.getName());
				} else {
					PL2.logger.warn(String.format("Failed to load method: %s, Valid Parameters: %s, Valid Returns %s,", method.toString(), validParams, validReturns));
				}
			}
		}
	}

	public void registerClientNames(String fieldName, List<String> fieldNames) {
		for (String name : fieldNames) {
			clientAdjustments.put(name, fieldName);
		}
	}

	public void registerFields(Class<?> classType, RegistryType type) {
		registerFields(classType, type, new ArrayList<>(), true);
	}

	public void registerFields(Class<?> classType, RegistryType type, List<String> fieldNames) {
		registerFields(classType, type, fieldNames, false);
	}

	public void registerFields(Class<?> classType, RegistryType type, List<String> fieldNames, boolean exclude) {
		validFields.putIfAbsent(type, new LinkedHashMap<>());
		validFields.get(type).putIfAbsent(classType, new ArrayList<>());
		Field[] fields = classType.getDeclaredFields();
		for (Field field : fields) {
			if ((fieldNames.isEmpty() || (exclude != fieldNames.contains(field.getName())))) {
				if (!field.isAccessible())
					field.setAccessible(true);
				boolean validReturns = isValidReturnType(field.getType());
				if (validReturns) {
					validFields.get(type).get(classType).add(field);
				} else {
					PL2.logger.warn(String.format("Failed to load field: %s, Valid Returns: %s,", field.toString(), validReturns));
				}
			}

		}
	}

	public void registerInvFields(Class<?> inventoryClass, Map<String, Integer> fields) {
		validInvFields.put(inventoryClass, fields);
	}

	public void registerInfoAdjustments(List<String> identifiers, String prefix, String suffix) {
		identifiers.forEach(identifier -> infoAdjustments.put(identifier, new Pair(prefix, suffix)));
	}

	public void registerInfoAdjustments(String identifier, String prefix, String suffix) {
		infoAdjustments.put(identifier, new Pair(prefix, suffix));
	}

	public boolean containsAssignableType(Class<?> toCheck, List<Class<?>> classes) {
		for (Class<?> cls : classes) {
			if (cls.isAssignableFrom(toCheck) || toCheck.isAssignableFrom(cls)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValidReturnType(Class<?> returnType) {
		return returnType.isPrimitive() || containsAssignableType(returnType, defaultReturns) || containsAssignableType(returnType, validReturns) || containsAssignableType(returnType, acceptedReturns);
	}

	public boolean validateParameters(Class<?>[] parameters) {
		if (parameters.length == 0) {
			return true;
		}
		for (Class<?> param : parameters) {
			if (!containsAssignableType(param, acceptedReturns)) {
				return false;
			}
		}
		return true;
	}

	public List<Method> getAssignableMethods(Class<?> obj, RegistryType type) {
		List<Method> methods = cachedMethods.get(obj);
		if (methods == null) {
			methods = new ArrayList<>();
			Map<Class<?>, List<Method>> map = validMethods.computeIfAbsent(type, m -> new LinkedHashMap<>());
			if (type == RegistryType.NONE) {
				map.putAll(validMethods.get(RegistryType.NONE));
			}
			for (Entry<Class<?>, List<Method>> classTypes : map.entrySet()) {
				if (classTypes.getKey().isAssignableFrom(obj) || obj.isAssignableFrom(classTypes.getKey())) {
					methods.addAll(classTypes.getValue());
				}
			}
			cachedMethods.put(obj, methods);
		}
		return methods;
	}

	public List<Field> getAccessibleFields(Class<?> obj, RegistryType type) {
		List<Field> fields = cachedFields.get(obj);
		if (fields == null) {
			fields = new ArrayList<>();
			Map<Class<?>, List<Field>> map = validFields.computeIfAbsent(type, m -> new LinkedHashMap<>());
			if (type == RegistryType.NONE) {
				map.putAll(validFields.get(RegistryType.NONE));
			}
			for (Entry<Class<?>, List<Field>> classTypes : map.entrySet()) {
				if (classTypes.getKey().isAssignableFrom(obj)) {
					fields.addAll(classTypes.getValue());
				}
			}
			cachedFields.put(obj, fields);
		}
		return fields;
	}

	public Object invokeMethod(Object obj, Method method, Object... available) {
		Class<?>[] params = method.getParameterTypes();
		Object[] inputs = new Object[params.length];
		for (int i = 0; i < params.length; i++) {
			Class<?> param = params[i];
			for (Object arg : available) {
				if (param.isInstance(arg)) {
					inputs[i] = arg;
					break;
				}
			}
		}
		for (Object input : inputs) {
			if (input == null) {
				return null;
			}
		}
		try {
			return method.invoke(obj, inputs);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			PL2.logger.debug("COULDN'T INVOKE METHOD! " + method + " on object " + obj);
		}
		return null;

	}

	public void getClassInfo(List<IProvidableInfo> infoList, LogicPath currentPath, RegistryType type, Object obj, Method method, Object... available) {
		Object returned = invokeMethod(obj, method, available);
		if (returned == null)
			return;
		Class<?> returnedClass = returned.getClass();
		currentPath.addObject(method);

		if (!returnedClass.isPrimitive() && !containsAssignableType(returnedClass, defaultReturns) && containsAssignableType(returnedClass, validReturns)) {
			getAssignableMethods(returnedClass, type).forEach(returnMethod -> getClassInfo(infoList, currentPath.dupe(), type, returned, returnMethod, available));
		} else {
			buildInfo(infoList, currentPath, getValidClassName(method.getDeclaringClass(), obj), method.getName(), type, returned);
		}
	}

	/** @param infoList the list to add to
	 * @param type the Registry Type to get the field from
	 * @param obj the object to get the field from
	 * @param field the field itself
	 * @param available all available info about the tile, typically will include the World, BlockPos, IBlockState, EnumFacing, the Block and the tile entity */
	public void getFieldInfo(List<IProvidableInfo> infoList, LogicPath currentPath, RegistryType type, Object obj, Field field, Object... available) {
		Object fieldObj = getField(obj, field);
		if (fieldObj == null)
			return;
		Class<?> returnedClass = fieldObj.getClass();
		currentPath.addObject(field);

		if (!returnedClass.isPrimitive() && !containsAssignableType(returnedClass, defaultReturns) && containsAssignableType(returnedClass, validReturns)) {
			getAssignableMethods(returnedClass, type).forEach(returnMethod -> getClassInfo(infoList, currentPath.dupe(), type, fieldObj, returnMethod, available));
			getAccessibleFields(returnedClass, type).forEach(subField -> getFieldInfo(infoList, currentPath.dupe(), type, fieldObj, subField, available));
		} else {
			buildInfo(infoList, currentPath.dupe(), getValidClassName(field.getDeclaringClass(), obj), field.getName(), type, fieldObj);
		}
	}

	public String getValidClassName(Class declared, Object obj) {
		if (declared == Enum.class) {
			return obj.getClass().getSimpleName();
		}
		return declared.getSimpleName();
	}

	/** @param infoList the list to add to
	 * @param className the name of the class
	 * @param fieldName the name of the method or field
	 * @param object the object returned, this will never be null and will be of compatible type */
	public void buildInfo(List<IProvidableInfo> infoList, LogicPath path, String className, String fieldName, RegistryType type, Object object) {
		path.setRegistryType(type);
		LogicInfo info = LogicInfo.buildDirectInfo(className + "." + fieldName, type, object).setPath(path);
		if (info != null) {
			infoList.add(info);
		}
	}

	/** @param obj the object to get the field from
	 * @param field the field to obtain
	 * @return the fields object if there is one */
	public Object getField(Object obj, Field field) {
		try {
			if (!field.isAccessible())
				field.setAccessible(true);
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<IProvidableInfo> getEntityInfo(List<IProvidableInfo> infoList, Entity entity) {
		Class<?> argClass;
		if (entity != null && containsAssignableType(argClass = entity.getClass(), acceptedReturns)) {
			LogicPath logicPath = new LogicPath();
			logicPath.setStart(entity);
			EnumFacing currentFace = null;
			RegistryType type = RegistryType.getRegistryType(argClass);
			getAssignableMethods(argClass, type).forEach(method -> getClassInfo(infoList, logicPath.dupe(), type, entity, method));
			getAccessibleFields(argClass, type).forEach(field -> getFieldInfo(infoList, logicPath.dupe(), type, entity, field));
			addCapabilities(infoList, logicPath.dupe(), entity, currentFace);
		}
		return infoList;
	}

	/** @param infoList the list to add to
	 * @param available all available info about the tile, typically will include the World, BlockPos, IBlockState, EnumFacing, the Block and the tile entity
	 * @return all the available info */
	public List<IProvidableInfo> getTileInfo(List<IProvidableInfo> infoList, EnumFacing currentFace, Object... available) {
		for (Object arg : available) {

			Class<?> argClass;
			if (arg != null && containsAssignableType(argClass = arg.getClass(), acceptedReturns)) {
				LogicPath currentPath = new LogicPath();
				currentPath.setStart(arg);
				RegistryType type = RegistryType.getRegistryType(argClass);
				getAssignableMethods(argClass, type).forEach(method -> getClassInfo(infoList, currentPath.dupe(), type, arg, method, available));
				getAccessibleFields(argClass, type).forEach(field -> getFieldInfo(infoList, currentPath.dupe(), type, arg, field));
				if (arg instanceof IInventory) {
					Map<String, Integer> fields = validInvFields.get(argClass);
					if (fields != null && !fields.isEmpty()) {
						fields.forEach((key, value) -> {
                            LogicPath invPath = currentPath.dupe();
                            invPath.addObject(new InventoryField(key, value, type));
                            invPath.setRegistryType(type);
                            infoList.add(LogicInfo.buildDirectInfo(argClass.getSimpleName() + "." + key, type, ((IInventory) arg).getField(value)).setPath(invPath));
                        });
					}
				}
				addCapabilities(infoList, currentPath.dupe(), arg, currentFace, available);
			}
		}
		return infoList;
	}

	public void addCapabilities(final List<IProvidableInfo> infoList, LogicPath path, Object obj, EnumFacing currentFace, Object... available) {
		if (obj instanceof ICapabilityProvider && !validCapabilities.isEmpty()) {
			ICapabilityProvider provider = (ICapabilityProvider) obj;
			List<Capability> capabilities = new ArrayList<>();
			for (Capability cap : validCapabilities) {
				if (provider.hasCapability(cap, currentFace)) {
					Capability providedCap = (Capability) provider.getCapability(cap, currentFace);
					if (providedCap != null) {
						capabilities.add(cap);
					}
				}
			}
			for (Capability cap : capabilities) {
				LogicPath logicPath = path.dupe();
				logicPath.addObject(new CapabilityMethod(cap));
				getAssignableMethods(cap.getClass(), RegistryType.CAPABILITY).forEach(method -> getClassInfo(infoList, logicPath.dupe(), RegistryType.CAPABILITY, cap, method, available));
				getAccessibleFields(cap.getClass(), RegistryType.CAPABILITY).forEach(field -> getFieldInfo(infoList, logicPath.dupe(), RegistryType.CAPABILITY, cap, field));
			}
		}
	}

	public <T extends IProvidableInfo> Pair<Boolean, T> getLatestInfo(AbstractChangeableList<T> updateInfo, List<NodeConnection> connections, T monitorInfo) {
		if (monitorInfo == null) {
			return null;
		}
		Pair<Boolean, T> newPaired = null;
		if (!connections.isEmpty()) {
			if (monitorInfo.getPath() == null) { // RESTORE PATH FOR SAVED INFO
				IMonitoredValue<T> latest = updateInfo.find(monitorInfo);
				// Object latest = updateInfo.getLatestInfo(info).b;
				if (latest != null) {
					IInfo saveableInfo = latest.getSaveableInfo();
					if (saveableInfo instanceof IProvidableInfo) {
						IProvidableInfo latestInfo = ((IProvidableInfo) saveableInfo);
						if (latestInfo.getPath() != null) {
							monitorInfo.setPath(latestInfo.getPath().dupe());
						}
					}
				}
			}
			// FIXME emergency path recovery - Happens when a list isn't being updated but the info in it is
			if (monitorInfo.getPath() == null) {
				NodeConnection connect = connections.get(0);
				InfoChangeableList list = InfoChangeableList.newChangeableList();
				if (connect instanceof BlockConnection) {
					list = InfoNetworkHandler.INSTANCE.updateInfo(null, list, (BlockConnection) connect);
				} else if (connect instanceof EntityConnection) {
					list = InfoNetworkHandler.INSTANCE.updateInfo(null, list, (EntityConnection) connect);
				}
				IMonitoredValue<T> latest = list.find(monitorInfo);
				if (latest != null) {
					IInfo saveableInfo = latest.getSaveableInfo();
					if (saveableInfo instanceof IProvidableInfo) {
						IProvidableInfo latestInfo = ((IProvidableInfo) saveableInfo);
						if (latestInfo.getPath() != null) {
							monitorInfo.setPath(latestInfo.getPath().dupe());
						}
					}
				}
				//what happens if we didn't get it???
			}
			newPaired = getLatestInfo(monitorInfo, connections.get(0));
		}
		if (newPaired == null) {
			IMonitoredValue<T> latest = updateInfo.find(monitorInfo);
			// boolean shouldUpdate = latest==null? false : latest.getChange().shouldUpdate();
			IInfo info = latest == null ? monitorInfo : latest.getSaveableInfo();
			newPaired = new Pair(true, info);
		}
		return newPaired;
	}

	public <T extends IProvidableInfo> Pair<Boolean, T> getLatestInfo(T info, NodeConnection entry) {
		if (info.getPath() == null) {
			return null;
		}
		IProvidableInfo returned = null;
		if (entry instanceof BlockConnection) {
			BlockConnection connection = (BlockConnection) entry;
			EnumFacing face = connection.face.getOpposite();
			World world = connection.coords.getWorld();
			IBlockState state = connection.coords.getBlockState(world);
			BlockPos pos = connection.coords.getBlockPos();
			Block block = state.getBlock();
			TileEntity tile = connection.coords.getTileEntity(world);
			returned = getInfoFromPath(info, info.getPath(), face, world, state, pos, face, block, tile);
		}
		if (entry instanceof EntityConnection) {
			EntityConnection connection = (EntityConnection) entry;
			Entity entity = connection.entity;
			World world = entity.getEntityWorld();
			returned = getInfoFromPath(info, info.getPath(), EnumFacing.NORTH, entity, world);
		}
		if (returned != null) {
			return new Pair(true, returned);
		}
		return null;
	}

	public <T extends IProvidableInfo> T getInfoFromPath(T info, LogicPath logicPath, EnumFacing currentFace, Object... available) {
		Object returned = logicPath.getStart(available);
		if (returned.equals(TileHandlerMethod.class)) {
			TileHandlerMethod method = (TileHandlerMethod) logicPath.startObj;
			LogicPath path = logicPath.dupe();
			List<T> infolist = new ArrayList<>();
			method.handler.provide(this, (List<IProvidableInfo>) infolist, path, method.bitCode, (World) available[0], (IBlockState) available[1], (BlockPos) available[2], (EnumFacing) available[3], (Block) available[4], (TileEntity) available[5]);
			for (T logicInfo : infolist) {
				if (logicInfo.isValid() && logicInfo.isMatchingType(info) && logicInfo.isMatchingInfo(info)) {
					return logicInfo; // should fix to use paths given in info if possible :P
				}
			}
		}
		for (Object arg : logicPath.path) {
			if (returned == null || arg == null) {
				return null;
			}

			if (arg instanceof Method) {
				returned = invokeMethod(returned, (Method) arg, available);
				continue;
			}
			if (arg instanceof Field) {
				returned = getField(returned, (Field) arg);
				continue;
			}
			if (arg instanceof CapabilityMethod) {
				if (returned instanceof ICapabilityProvider) {
					returned = ((ICapabilityProvider) returned).getCapability(((CapabilityMethod) arg).cap, currentFace);
					continue;
				} else {
					return null;
				}
			}
			if (arg instanceof InventoryField && returned instanceof IInventory) {
				InventoryField field = ((InventoryField) arg);
				info.setFromReturn(logicPath, ((IInventory) returned).getField(field.value));
				return info;
			}
		}
		if (returned != null && ObjectType.getInfoType(returned) != ObjectType.NONE) {
			info.setFromReturn(logicPath, returned);
		}
		return info;
	}

}
