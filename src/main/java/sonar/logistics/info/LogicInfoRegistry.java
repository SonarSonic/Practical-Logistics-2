package sonar.logistics.info;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Loader;
import sonar.core.utils.Pair;
import sonar.logistics.Logistics;
import sonar.logistics.api.info.ICustomEntityHandler;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.info.types.LogicInfo;

/** where all the registering for LogicInfo happens */
public class LogicInfoRegistry {

	/** the cache of methods/fields applicable to a given tile. */
	public static LinkedHashMap<Class<?>, ArrayList<Method>> cachedMethods = new LinkedHashMap();
	public static LinkedHashMap<Class<?>, ArrayList<Field>> cachedFields = new LinkedHashMap();

	/** all the registries which can provide valid returns, methods and fields */
	public static ArrayList<IInfoRegistry> infoRegistries = new ArrayList();

	/** all custom handlers which can provide custom info on blocks for tricky situations */
	public static ArrayList<ICustomTileHandler> customTileHandlers = new ArrayList();
	public static ArrayList<ICustomEntityHandler> customEntityHandlers = new ArrayList();

	/** all the register validated returns, methods and fields from the registries */
	public static ArrayList<Class<?>> registeredReturnTypes = Lists.newArrayList();
	public static ArrayList<Capability> registeredCapabilities = Lists.newArrayList();
	public static LinkedHashMap<RegistryType, LinkedHashMap<Class<?>, ArrayList<Method>>> infoMethods = new LinkedHashMap();
	public static LinkedHashMap<RegistryType, LinkedHashMap<Class<?>, ArrayList<Field>>> infoFields = new LinkedHashMap();
	public static LinkedHashMap<Class<?>, Map<String, Integer>> invFields = new LinkedHashMap();
	public static LinkedHashMap<String, Pair<String, String>> infoAdjustments = new LinkedHashMap();
	public static LinkedHashMap<String, String> clientNameAdjustments = new LinkedHashMap(); // to give other methods the lang id of others

	/** the default accepted returns */
	public static ArrayList<Class<?>> acceptedTypes = RegistryType.buildArrayList();
	public static ArrayList<Class<?>> defaultReturnTypes = Lists.newArrayList(String.class);

	/** used to define the type of class the method/return is applicable for this is to speed up identification but you can use NONE for any type of class if you wish */
	public static enum RegistryType {
		WORLD(World.class, 0), TILE(TileEntity.class, 5), BLOCK(Block.class, 3), ENTITY(Entity.class, 6), ITEM(Item.class, 7), STATE(IBlockState.class, 4), POS(BlockPos.class, 1), FACE(EnumFacing.class, 2), ITEMSTACK(ItemStack.class, 8), CAPABILITY(Capability.class, 9), NONE(null, 9);
		Class classType;
		public int sortOrder;

		RegistryType(Class classType, int sortOrder) {
			this.classType = classType;
			this.sortOrder = sortOrder;
		}

		public boolean isAssignable(Class<?> toCheck) {
			return classType != null && classType.isAssignableFrom(toCheck);
		}

		public static RegistryType getRegistryType(Class<?> toCheck) {
			for (RegistryType type : values()) {
				if (type.isAssignable(toCheck)) {
					return type;
				}
			}
			return NONE;
		}

		public static ArrayList<Class<?>> buildArrayList() {
			ArrayList<Class<?>> classes = new ArrayList();
			for (RegistryType type : values()) {
				if (type.classType != null) {
					classes.add(type.classType);
				}
			}
			return classes;
		}
	}

	public static void init() {

		infoRegistries.forEach(registry -> {
			try {
				registry.registerBaseReturns();
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
			}
		});
		infoRegistries.forEach(registry -> {
			try {
				registry.registerBaseMethods();
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
			}
		});
		infoRegistries.forEach(registry -> {
			try {
				registry.registerAllFields();
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
			}
		});
		infoRegistries.forEach(registry -> {
			try {
				registry.registerAdjustments();
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
			}
		});
	}

	public static void reload() {
		registeredReturnTypes.clear();
		infoMethods.clear();
		infoFields.clear();
		invFields.clear();
		infoAdjustments.clear();
		clientNameAdjustments.clear();

		init();

		cachedFields.clear();
		cachedMethods.clear();
	}

	public static void registerInfoRegistry(String modid, IInfoRegistry handler) {
		if (Loader.isModLoaded(modid)) {
			infoRegistries.add(handler);
		}
	}

	public static void registerCapability(Capability capability) {
		registeredCapabilities.add(capability);
	}

	public static void registerReturn(Class<?> classType) {
		registeredReturnTypes.add(classType);
	}

	public static void registerMethods(Class<?> classType, RegistryType type) {
		registerMethods(classType, type, Lists.newArrayList());
	}

	public static void registerMethods(Class<?> classType, RegistryType type, ArrayList<String> methodNames) {
		registerMethods(classType, type, methodNames, false);
	}

	public static void registerMethods(Class<?> classType, RegistryType type, ArrayList<String> methodNames, boolean exclude) {
		infoMethods.putIfAbsent(type, new LinkedHashMap());
		infoMethods.get(type).putIfAbsent(classType, new ArrayList());
		ArrayList<String> used = new ArrayList();
		Method[] methods = classType.getMethods();
		for (Method method : methods) {
			if (!used.contains(method.getName()) && (methodNames.isEmpty() || (exclude ? !methodNames.contains(method.getName()) : methodNames.contains(method.getName())))) {
				boolean validParams = validateParameters(method.getParameterTypes()), validReturns = isValidReturnType(method.getReturnType());
				if (validParams && validReturns) {
					infoMethods.get(type).get(classType).add(method);
					used.add(method.getName());
				} else {
					Logistics.logger.error(String.format("Failed to load method: %s, Valid Parameters: %s, Valid Returns %s,", method.toString(), validParams, validReturns));
				}
			}
		}
	}

	public static void registerClientNames(String fieldName, ArrayList<String> fieldNames) {
		for (String name : fieldNames) {
			clientNameAdjustments.put(name, fieldName);
		}
	}

	public static void registerFields(Class<?> classType, RegistryType type) {
		registerFields(classType, type, Lists.newArrayList());
	}

	public static void registerFields(Class<?> classType, RegistryType type, ArrayList<String> fieldNames) {
		registerFields(classType, type, fieldNames, false);
	}

	public static void registerFields(Class<?> classType, RegistryType type, ArrayList<String> fieldNames, boolean exclude) {
		infoFields.putIfAbsent(type, new LinkedHashMap());
		infoFields.get(type).putIfAbsent(classType, new ArrayList());
		Field[] fields = classType.getFields();
		for (Field field : fields) {
			if ((fieldNames.isEmpty() || (exclude ? !fieldNames.contains(field.getName()) : fieldNames.contains(field.getName())))) {
				boolean validReturns = isValidReturnType(field.getType());
				if (validReturns) {
					infoFields.get(type).get(classType).add(field);
				} else {
					Logistics.logger.error(String.format("Failed to load field: %s, Valid Returns: %s,", field.toString(), validReturns));
				}
			}

		}
	}

	public static void registerInvFields(Class<?> inventoryClass, Map<String, Integer> fields) {
		invFields.put(inventoryClass, fields);
	}

	public static void registerInfoAdjustments(ArrayList<String> identifiers, String prefix, String suffix) {
		identifiers.forEach(identifier -> infoAdjustments.put(identifier, new Pair(prefix, suffix)));
	}

	public static void registerInfoAdjustments(String identifier, String prefix, String suffix) {
		infoAdjustments.put(identifier, new Pair(prefix, suffix));
	}

	public static boolean containsAssignableType(Class<?> toCheck, ArrayList<Class<?>> classes) {
		for (Class<?> cls : classes) {
			if (cls.isAssignableFrom(toCheck) || toCheck.isAssignableFrom(cls)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isValidReturnType(Class<?> returnType) {
		return returnType.isPrimitive() || containsAssignableType(returnType, defaultReturnTypes) || containsAssignableType(returnType, registeredReturnTypes) || containsAssignableType(returnType, acceptedTypes);
	}

	public static boolean validateParameters(Class<?>[] parameters) {
		if (parameters.length == 0) {
			return true;
		}
		for (Class<?> param : parameters) {
			if (!containsAssignableType(param, acceptedTypes)) {
				return false;
			}
		}
		return true;
	}

	public static ArrayList<Method> getAssignableMethods(Class<?> obj, RegistryType type) {
		ArrayList<Method> methods = cachedMethods.get(obj);
		if (methods == null) {
			methods = new ArrayList();
			LinkedHashMap<Class<?>, ArrayList<Method>> map = infoMethods.getOrDefault(type, new LinkedHashMap());
			if (type == RegistryType.NONE) {
				map.putAll(infoMethods.get(RegistryType.NONE));
			}
			for (Entry<Class<?>, ArrayList<Method>> classTypes : map.entrySet()) {
				if (classTypes.getKey().isAssignableFrom(obj) || obj.isAssignableFrom(classTypes.getKey())) {
					methods.addAll(classTypes.getValue());
				}
			}
			cachedMethods.put(obj, methods);
		}
		return methods;
	}

	public static ArrayList<Field> getAccessibleFields(Class<?> obj, RegistryType type) {
		ArrayList<Field> fields = cachedFields.get(obj);
		if (fields == null) {
			fields = new ArrayList();
			LinkedHashMap<Class<?>, ArrayList<Field>> map = infoFields.getOrDefault(type, new LinkedHashMap());
			if (type == RegistryType.NONE) {
				map.putAll(infoFields.get(RegistryType.NONE));
			}
			for (Entry<Class<?>, ArrayList<Field>> classTypes : map.entrySet()) {
				if (classTypes.getKey().isAssignableFrom(obj)) {
					fields.addAll(classTypes.getValue());
				}
			}
			cachedFields.put(obj, fields);
		}
		return fields;
	}

	public static Object invokeMethod(Object obj, Method method, Object... available) {
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
			Logistics.logger.error("COULDN'T INVOKE METHOD! " + method + " on object " + obj);
		}
		return null;

	}

	public static void getClassInfo(List<LogicInfo> infoList, LogicPath currentPath, RegistryType type, Object obj, Method method, Object... available) {
		Object returned = invokeMethod(obj, method, available);
		if (returned == null)
			return;
		Class<?> returnedClass = returned.getClass();
		currentPath.addObject(method);

		if (!returnedClass.isPrimitive() && !containsAssignableType(returnedClass, defaultReturnTypes) && containsAssignableType(returnedClass, registeredReturnTypes)) {
			getAssignableMethods(returnedClass, type).forEach(returnMethod -> getClassInfo(infoList, currentPath.dupe(), type, returned, returnMethod, available));
		} else {
			buildInfo(infoList, currentPath, method.getDeclaringClass().getSimpleName(), method.getName(), type, returned);
		}
	}

	/** @param infoList the list to add to
	 * @param type the Registry Type to get the field from
	 * @param obj the object to get the field from
	 * @param field the field itself
	 * @param available all available info about the tile, typically will include the World, BlockPos, IBlockState, EnumFacing, the Block and the tile entity */
	public static void getFieldInfo(List<LogicInfo> infoList, LogicPath currentPath, RegistryType type, Object obj, Field field, Object... available) {
		Object fieldObj = getField(obj, field);
		if (fieldObj == null)
			return;
		Class<?> returnedClass = fieldObj.getClass();
		currentPath.addObject(field);

		if (!returnedClass.isPrimitive() && !containsAssignableType(returnedClass, defaultReturnTypes) && containsAssignableType(returnedClass, registeredReturnTypes)) {
			getAssignableMethods(returnedClass, type).forEach(returnMethod -> getClassInfo(infoList, currentPath.dupe(), type, fieldObj, returnMethod, available));
		} else {
			buildInfo(infoList, currentPath.dupe(), field.getDeclaringClass().getSimpleName(), field.getName(), type, fieldObj);
		}
	}

	/** @param infoList the list to add to
	 * @param className the name of the class
	 * @param fieldName the name of the method or field
	 * @param object the object returned, this will never be null and will be of compatible type */
	public static void buildInfo(List<LogicInfo> infoList, LogicPath path, String className, String fieldName, RegistryType type, Object object) {
		path.setRegistryType(type);
		LogicInfo info = LogicInfo.buildDirectInfo(className + "." + fieldName, type, object, path);
		if (info != null) {
			infoList.add(info);
		}
	}

	/** @param obj the object to get the field from
	 * @param field the field to obtain
	 * @return the fields object if there is one */
	public static Object getField(Object obj, Field field) {
		try {
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<LogicInfo> getEntityInfo(final List<LogicInfo> infoList, Entity entity) {
		Class<?> argClass;
		if (entity != null && containsAssignableType(argClass = entity.getClass(), acceptedTypes)) {
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
	public static List<LogicInfo> getTileInfo(final List<LogicInfo> infoList, EnumFacing currentFace, Object... available) {
		for (Object arg : available) {

			Class<?> argClass;
			if (arg != null && containsAssignableType(argClass = arg.getClass(), acceptedTypes)) {
				LogicPath currentPath = new LogicPath();
				currentPath.setStart(arg);
				RegistryType type = RegistryType.getRegistryType(argClass);
				getAssignableMethods(argClass, type).forEach(method -> getClassInfo(infoList, currentPath.dupe(), type, arg, method, available));
				getAccessibleFields(argClass, type).forEach(field -> getFieldInfo(infoList, currentPath.dupe(), type, arg, field));
				if (arg instanceof IInventory) {
					Map<String, Integer> fields = invFields.get(argClass);
					if (fields != null && !fields.isEmpty()) {
						fields.entrySet().forEach(field -> {
							LogicPath invPath = currentPath.dupe();
							invPath.addObject(new InvField(field.getKey(), field.getValue(), type));
							invPath.setRegistryType(type);
							infoList.add(LogicInfo.buildDirectInfo(argClass.getSimpleName() + "." + field.getKey(), type, ((IInventory) arg).getField(field.getValue()), invPath));
						});
					}
				}
				addCapabilities(infoList, currentPath.dupe(), arg, currentFace, available);
			}
		}
		return infoList;
	}

	public static void addCapabilities(final List<LogicInfo> infoList, LogicPath path, Object obj, EnumFacing currentFace, Object... available) {
		if (obj instanceof ICapabilityProvider && !registeredCapabilities.isEmpty()) {
			ICapabilityProvider provider = (ICapabilityProvider) obj;
			ArrayList<Capability> capabilities = new ArrayList();
			for (Capability cap : registeredCapabilities) {
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

	public static Pair<Boolean, IMonitorInfo> getLatestInfo(MonitoredList updateInfo, ArrayList<NodeConnection> connections, IMonitorInfo paired) {
		Pair<Boolean, IMonitorInfo> newPaired = null;
		if (paired instanceof LogicInfo && !connections.isEmpty()) {
			LogicInfo info = (LogicInfo) paired;
			if (info.path == null) {
				Object latest = updateInfo.getLatestInfo(info).b;
				if (latest != null && latest instanceof LogicInfo) {
					LogicInfo latestInfo = ((LogicInfo) latest);
					if (latestInfo.path != null) {
						info.path = latestInfo.path.dupe();
					}
				}
			}
			newPaired = LogicInfoRegistry.getLatestInfo(info, connections.get(0));
		}
		if (newPaired == null) {
			newPaired = updateInfo.getLatestInfo(paired);
		}
		return newPaired;
	}

	public static Pair<Boolean, IMonitorInfo> getLatestInfo(LogicInfo info, NodeConnection entry) {
		if (info.path == null) {
			return null;
		}
		LogicInfo returned = null;
		if (entry instanceof BlockConnection) {
			BlockConnection connection = (BlockConnection) entry;
			EnumFacing face = connection.face.getOpposite();
			World world = connection.coords.getWorld();
			IBlockState state = connection.coords.getBlockState(world);
			BlockPos pos = connection.coords.getBlockPos();
			Block block = state.getBlock();
			TileEntity tile = connection.coords.getTileEntity(world);
			returned = LogicInfoRegistry.getLogicInfoFromPath(info, info.path, face, world, state, pos, face, block, tile);
		}
		if (entry instanceof EntityConnection) {
			EntityConnection connection = (EntityConnection) entry;
			Entity entity = connection.entity;
			World world = entity.getEntityWorld();
			returned = LogicInfoRegistry.getLogicInfoFromPath(info, info.path, EnumFacing.NORTH, entity, world);
		}
		if (returned != null) {
			return new Pair(true, returned);
		}
		return null;
	}

	public static LogicInfo getLogicInfoFromPath(LogicInfo info, LogicPath logicPath, EnumFacing currentFace, Object... available) {

		Object returned = logicPath.getStart(available);
		if (returned instanceof TileHandlerMethod) {
			TileHandlerMethod method = (TileHandlerMethod) returned;
			LogicPath path = new LogicPath();
			path.setStart(method);
			ArrayList<LogicInfo> infolist = new ArrayList();
			method.handler.addInfo(infolist, path, (World) available[0], (IBlockState) available[1], (BlockPos) available[2], (EnumFacing) available[3], (TileEntity) available[4], (Block) available[5]);
			for (LogicInfo logicInfo : infolist) {
				if (logicInfo.isValid() && logicInfo.isMatchingType(info) && logicInfo.isMatchingInfo(info)) {
					return logicInfo; // should fix to use paths given in info if possible :P
				}
			}

		}
		for (Object arg : logicPath.path) {
			if (returned == null) {
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
			if (arg instanceof InvField && returned instanceof IInventory) {
				InvField field = ((InvField) arg);
				info.obj.obj = ((IInventory) returned).getField(field.value);
				return info;
			}
		}
		if (returned != null)
			info.obj.obj = returned;
		return info;
	}

	public static class LogicPath {
		ArrayList path = new ArrayList();
		Class<?> start;
		RegistryType type;

		public void setRegistryType(RegistryType regType) {
			type = regType;
		}

		public void setStart(Object obj) {
			start = obj.getClass();
		}

		public void addObject(Object obj) {
			path.add(obj);
		}

		public Object getStart(Object... available) {
			for (Object arg : available) {
				if (start.isInstance(arg)) {
					return arg;
				}
			}
			return start; // lets hope this doesn't happen to often :P
		}

		public LogicPath dupe() {
			LogicPath path = new LogicPath();
			path.path = (ArrayList) this.path.clone();
			path.start = start;
			return path;
		}
	}

	public static class CapabilityMethod {

		public Capability cap;

		public CapabilityMethod(Capability cap) {
			this.cap = cap;
		}
	}

	public static class InvField {

		public String key;
		public Integer value;
		public RegistryType type;

		public InvField(String key, Integer value, RegistryType type) {
			this.key = key;
			this.value = value;
			this.type = type;
		}
	}

	public static class TileHandlerMethod {
		ICustomTileHandler handler;

		public TileHandlerMethod(ICustomTileHandler handler) {
			this.handler = handler;
		}
	}
}
