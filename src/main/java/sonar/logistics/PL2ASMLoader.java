package sonar.logistics;

import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import sonar.core.helpers.ASMLoader;
import sonar.core.utils.Pair;
import sonar.logistics.api.asm.DisplayActionType;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.asm.EntityInfoProvider;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.asm.LogicComparator;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.asm.LogicListSorter;
import sonar.logistics.api.asm.NodeFilter;
import sonar.logistics.api.asm.StyledStringType;
import sonar.logistics.api.asm.TileInfoProvider;
import sonar.logistics.api.displays.IDisplayAction;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.text.IStyledString;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.handlers.IEntityInfoProvider;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.tiles.readers.ILogicListSorter;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.info.comparators.ILogicComparator;

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

	
	
}
