package sonar.logistics;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.ASMLoader;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.utils.Pair;
import sonar.logistics.api.asm.*;
import sonar.logistics.api.displays.IDisplayAction;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.text.IStyledString;
import sonar.logistics.api.errors.IInfoError;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.handlers.IEntityInfoProvider;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.tiles.readers.ILogicListSorter;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.info.comparators.ILogicComparator;

import javax.annotation.Nonnull;
import java.util.*;

public class PL2ASMLoader {

	public static LinkedHashMap<Integer, String> infoNames = new LinkedHashMap<>();
	public static LinkedHashMap<String, Integer> infoIds = new LinkedHashMap<>();
	public static LinkedHashMap<String, Class<? extends IInfo>> infoClasses = new LinkedHashMap<>();

	public static LinkedHashMap<Integer, String> elementNames = new LinkedHashMap<>();
	public static LinkedHashMap<String, Integer> elementIDs = new LinkedHashMap<>();
	public static LinkedHashMap<Integer, Class<? extends IDisplayElement>> elementIClasses = new LinkedHashMap<>();
	public static LinkedHashMap<String, Class<? extends IDisplayElement>> elementSClasses = new LinkedHashMap<>();

	public static LinkedHashMap<Integer, String> sstringNames = new LinkedHashMap<>();
	public static LinkedHashMap<String, Integer> sstringIDs = new LinkedHashMap<>();
	public static LinkedHashMap<Integer, Class<? extends IStyledString>> sstringIClasses = new LinkedHashMap<>();
	public static LinkedHashMap<String, Class<? extends IStyledString>> sstringSClasses = new LinkedHashMap<>();

	public static LinkedHashMap<Integer, String> comparatorNames = new LinkedHashMap<>();
	public static LinkedHashMap<String, Integer> comparatorIds = new LinkedHashMap<>();
	public static LinkedHashMap<String, ILogicComparator> comparatorClasses = new LinkedHashMap<>();

	public static LinkedHashMap<Integer, String> displayActionNames = new LinkedHashMap<>();
	public static LinkedHashMap<String, Integer> displayActionIDs = new LinkedHashMap<>();
	public static LinkedHashMap<Integer, Class<? extends IDisplayAction>> displayActionIClasses = new LinkedHashMap<>();
	public static LinkedHashMap<String, Class<? extends IDisplayAction>> displayActionSClasses = new LinkedHashMap<>();

	public static LinkedHashMap<Integer, String> changeableListSorterNames = new LinkedHashMap<>();
	public static LinkedHashMap<String, Integer> changeableListSorterIDs = new LinkedHashMap<>();
	public static LinkedHashMap<Integer, Class<? extends ILogicListSorter>> changeableListSorterIClasses = new LinkedHashMap<>();
	public static LinkedHashMap<String, Class<? extends ILogicListSorter>> changeableListSorterSClasses = new LinkedHashMap<>();

	public static LinkedHashMap<String, Class<? extends INodeFilter>> filterClasses = new LinkedHashMap<>();

	private PL2ASMLoader() {}

	public static void init(FMLPreInitializationEvent event) {
		ASMDataTable asmDataTable = event.getAsmData();
		PL2ASMLoader.loadInfoTypes(asmDataTable);
		PL2ASMLoader.loadDisplayElementTypes(asmDataTable);
		PL2ASMLoader.loadDisplayActionTypes(asmDataTable);
		PL2ASMLoader.loadStyledStringTypes(asmDataTable);
		PL2ASMLoader.loadLogicListSorters(asmDataTable);
		PL2ASMLoader.loadComparatorTypes(asmDataTable);
		PL2ASMLoader.loadNodeFilters(asmDataTable);
		LogicInfoRegistry.INSTANCE.infoRegistries.addAll(PL2ASMLoader.getInfoRegistries(asmDataTable));
		LogicInfoRegistry.INSTANCE.tileProviders.addAll(PL2ASMLoader.getTileProviders(asmDataTable));
		LogicInfoRegistry.INSTANCE.entityProviders.addAll(PL2ASMLoader.getEntityProviders(asmDataTable));
		PL2ASMLoader.loadASMLoadable(asmDataTable, IInfoError.class, InfoErrorType.class, "IEuiD", true);
	}

	public static List<IInfoRegistry> getInfoRegistries(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(PL2.logger, asmDataTable, InfoRegistry.class, IInfoRegistry.class, true, false);
	}

	public static List<ITileInfoProvider> getTileProviders(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(PL2.logger, asmDataTable, TileInfoProvider.class, ITileInfoProvider.class, true, false);
	}

	public static List<IEntityInfoProvider> getEntityProviders(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(PL2.logger, asmDataTable, EntityInfoProvider.class, IEntityInfoProvider.class, true, false);
	}

	public static void loadComparatorTypes(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends ILogicComparator>>> infoTypes = ASMLoader.getClasses(PL2.logger, asmDataTable, LogicComparator.class, ILogicComparator.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends ILogicComparator>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			int hashCode = name.hashCode();
			comparatorNames.put(hashCode, name);
			comparatorIds.put(name, hashCode);
			try {
				comparatorClasses.put(name, info.b.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		PL2.logger.info("Loaded: " + comparatorIds.size() + " Comparator Types");
	}

	public static void loadInfoTypes(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends IInfo>>> infoTypes = ASMLoader.getClasses(PL2.logger, asmDataTable, LogicInfoType.class, IInfo.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends IInfo>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			int hashCode = name.hashCode();
			infoNames.put(hashCode, name);
			infoIds.put(name, hashCode);
			infoClasses.put(name, info.b);
		}
		PL2.logger.info("Loaded: " + infoIds.size() + " Info Types");
	}

	public static void loadDisplayElementTypes(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends IDisplayElement>>> infoTypes = ASMLoader.getClasses(PL2.logger, asmDataTable, DisplayElementType.class, IDisplayElement.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends IDisplayElement>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			int hashCode = name.hashCode();
			elementNames.put(hashCode, name);
			elementIDs.put(name, hashCode);
			elementSClasses.put(name, info.b);
			elementIClasses.put(hashCode, info.b);
		}
		PL2.logger.info("Loaded: " + elementIDs.size() + " Element Types");
	}

	public static void loadStyledStringTypes(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends IStyledString>>> infoTypes = ASMLoader.getClasses(PL2.logger, asmDataTable, StyledStringType.class, IStyledString.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends IStyledString>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			int hashCode = name.hashCode();
			sstringNames.put(hashCode, name);
			sstringIDs.put(name, hashCode);
			sstringSClasses.put(name, info.b);
			sstringIClasses.put(hashCode, info.b);
		}
		PL2.logger.info("Loaded: " + sstringIDs.size() + " Styled String Types");
	}

	public static void loadDisplayActionTypes(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends IDisplayAction>>> infoTypes = ASMLoader.getClasses(PL2.logger, asmDataTable, DisplayActionType.class, IDisplayAction.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends IDisplayAction>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			int hashCode = name.hashCode();
			displayActionNames.put(hashCode, name);
			displayActionIDs.put(name, hashCode);
			displayActionSClasses.put(name, info.b);
			displayActionIClasses.put(hashCode, info.b);
		}
		PL2.logger.info("Loaded: " + displayActionIDs.size() + " Display Actions");
	}

	public static void loadLogicListSorters(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends ILogicListSorter>>> infoTypes = ASMLoader.getClasses(PL2.logger, asmDataTable, LogicListSorter.class, ILogicListSorter.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends ILogicListSorter>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			int hashCode = name.hashCode();
			changeableListSorterNames.put(hashCode, name);
			changeableListSorterIDs.put(name, hashCode);
			changeableListSorterSClasses.put(name, info.b);
			changeableListSorterIClasses.put(hashCode, info.b);
		}
		PL2.logger.info("Loaded: " + changeableListSorterIDs.size() + " Logic List Sorter");
	}

	public static void loadNodeFilters(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends INodeFilter>>> infoTypes = ASMLoader.getClasses(PL2.logger, asmDataTable, NodeFilter.class, INodeFilter.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends INodeFilter>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			filterClasses.put(name, info.b);
		}
		PL2.logger.info("Loaded: " + filterClasses.size() + " Filters");
	}

	public static Map<Class, Map<Integer, Class<? extends INBTSyncable>>> asm_loadables_iC = new HashMap<>();
	public static Map<Class, Map<Class<? extends INBTSyncable>, Integer>> asm_loadables_cI = new HashMap<>();
	public static Map<Class, String> asm_loadables_nbt = new HashMap<>();

	public static int loadASMLoadable(@Nonnull ASMDataTable asmDataTable, Class type, Class asmClass, String nbtString, boolean checkModid) {
		List<Pair<ASMData, Class>> infoTypes = ASMLoader.getClasses(PL2.logger, asmDataTable, asmClass, type, checkModid);
		Map<Integer, Class<? extends INBTSyncable>> loadables_iC = new HashMap<>();
		Map<Class<? extends INBTSyncable>, Integer> loadables_cI = new HashMap<>();
		for (Pair<ASMDataTable.ASMData, Class> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			int hashCode = getRegisteredID(name);
			if (loadables_iC.containsKey(hashCode)) {
				PL2.logger.error("DUPLICATE ID: " + name + " shared by " + info.b.getName() + " & " + loadables_iC.get(hashCode).getName());
			} else {
				loadables_iC.put(hashCode, info.b);
				loadables_cI.put(info.b, hashCode);
			}
		}
		asm_loadables_iC.put(type, loadables_iC);
		asm_loadables_cI.put(type, loadables_cI);
		asm_loadables_nbt.put(type, nbtString);
		return loadables_iC.size();
	}

	public static <T> int getRegisteredID(String id) {
		return id.hashCode();
	}

	public static <T> int getRegisteredID(Class<T> type, Class<? extends T> clazz) {
		return asm_loadables_cI.get(type).get(clazz);
	}

	public static <T extends INBTSyncable> Class<T> getRegisteredClass(Class<T> type, int id) {
		return (Class<T>) asm_loadables_iC.get(type).get(id);
	}

	public static <T extends INBTSyncable> Class<T> getRegisteredClass(Class<T> type, String id) {
		return (Class<T>) asm_loadables_iC.get(type).get(id.hashCode());
	}

	public static <T extends INBTSyncable> T instanceClass(Class<T> type, int id) {
		Class<T> clazz = getRegisteredClass(type, id);
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T extends INBTSyncable> T readFromNBT(Class<T> type, NBTTagCompound tag) {
		String stringID = asm_loadables_nbt.get(type);
		T instance = instanceClass(type, tag.getInteger(stringID));
		if (instance != null) {
			instance.readData(tag, SyncType.SAVE);
		}
		return instance;
	}

	public static <T extends INBTSyncable> List<T> readListFromNBT(Class<T> type, NBTTagCompound tag) {
		List<T> loaded = new ArrayList<>();
		String s = asm_loadables_nbt.get(type);
		NBTTagList list = tag.getTagList(s + "_list", NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound subTag = list.getCompoundTagAt(i);
			loaded.add(readFromNBT(type, subTag));
		}
		return loaded;
	}

	public static <T extends INBTSyncable> NBTTagCompound writeToNBT(Class<T> type, T instance, NBTTagCompound tag) {
		int id = getRegisteredID(type, (Class<? extends T>) instance.getClass());
		tag.setInteger(asm_loadables_nbt.get(type), id);
		instance.writeData(tag, SyncType.SAVE);
		return tag;
	}

	public static <T extends INBTSyncable> NBTTagCompound writeListToNBT(Class<T> type, List<T> instances, NBTTagCompound tag) {
		NBTTagList list = new NBTTagList();
		String s = asm_loadables_nbt.get(type);
		instances.forEach(i -> list.appendTag(writeToNBT(type, i, new NBTTagCompound())));
		tag.setTag(s + "_list", list);
		return tag;
	}

}
