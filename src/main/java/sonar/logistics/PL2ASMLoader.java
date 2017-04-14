package sonar.logistics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import sonar.core.helpers.ASMLoader;
import sonar.core.utils.Pair;
import sonar.logistics.api.asm.EntityInfoProvider;
import sonar.logistics.api.asm.EntityMonitorHandler;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.asm.NodeFilter;
import sonar.logistics.api.asm.TileInfoProvider;
import sonar.logistics.api.asm.NetworkHandler;
import sonar.logistics.api.asm.NetworkHandlerField;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.handlers.IEntityInfoProvider;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.logic.comparators.ILogicComparator;

public class PL2ASMLoader {

	public static LinkedHashMap<Integer, String> infoNames = Maps.newLinkedHashMap();
	public static LinkedHashMap<String, Integer> infoIds = Maps.newLinkedHashMap();
	public static LinkedHashMap<String, Class<? extends IInfo>> infoClasses = Maps.newLinkedHashMap();

	// public static LinkedHashMap<Integer, String> infoNames = Maps.newLinkedHashMap();
	// public static LinkedHashMap<String, Integer> infoIds = Maps.newLinkedHashMap();
	public static LinkedHashMap<String, Class<? extends INodeFilter>> filterClasses = Maps.newLinkedHashMap();
	public static LinkedHashMap<String, ILogicComparator> comparatorClasses = Maps.newLinkedHashMap();

	public static LinkedHashMap<String, INetworkHandler> networkHandlers = Maps.newLinkedHashMap();

	private PL2ASMLoader() {
	}

	public static void init(FMLPreInitializationEvent event) {
		ASMDataTable asmDataTable = event.getAsmData();
		PL2ASMLoader.loadInfoTypes(asmDataTable);
		PL2ASMLoader.loadNetworkHandlers(asmDataTable);
		PL2ASMLoader.loadNodeFilters(asmDataTable);
		LogicInfoRegistry.INSTANCE.infoRegistries.addAll(PL2ASMLoader.getInfoRegistries(asmDataTable));
		LogicInfoRegistry.INSTANCE.tileProviders.addAll(PL2ASMLoader.getTileProviders(asmDataTable));
		LogicInfoRegistry.INSTANCE.entityProviders.addAll(PL2ASMLoader.getEntityProviders(asmDataTable));
	}

	public static List<IInfoRegistry> getInfoRegistries(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(asmDataTable, InfoRegistry.class, IInfoRegistry.class, true, false);
	}

	public static List<ITileInfoProvider> getTileProviders(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(asmDataTable, TileInfoProvider.class, ITileInfoProvider.class, true, false);
	}

	public static List<IEntityInfoProvider> getEntityProviders(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(asmDataTable, EntityInfoProvider.class, IEntityInfoProvider.class, true, false);
	}

	public static void loadNetworkHandlers(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends INetworkHandler>>> infoTypes = ASMLoader.getClasses(asmDataTable, NetworkHandler.class, INetworkHandler.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends INetworkHandler>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("handlerID");
			try {
				networkHandlers.put(name, info.b.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				PL2.logger.error("FAILED: To Load Network Handler - " + name);
			}
		}
		PL2.logger.info("Loaded: " + networkHandlers.size() + " Network Handlers");

		ASMLoader.injectInstances(asmDataTable, NetworkHandlerField.class, INetworkHandler.class, asm -> {
			String name = (String) asm.getAnnotationInfo().get("handlerID");
			return networkHandlers.get(name);
		});

	}

	public static void loadInfoTypes(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends IInfo>>> infoTypes = ASMLoader.getClasses(asmDataTable, LogicInfoType.class, IInfo.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends IInfo>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			int hashCode = name.hashCode();
			infoNames.put(hashCode, name);
			infoIds.put(name, hashCode);
			infoClasses.put(name, info.b);
		}
		PL2.logger.info("Loaded: " + infoIds.size() + " Info Types");
	}

	public static void loadNodeFilters(@Nonnull ASMDataTable asmDataTable) {
		List<Pair<ASMDataTable.ASMData, Class<? extends INodeFilter>>> infoTypes = ASMLoader.getClasses(asmDataTable, NodeFilter.class, INodeFilter.class, true);
		for (Pair<ASMDataTable.ASMData, Class<? extends INodeFilter>> info : infoTypes) {
			String name = (String) info.a.getAnnotationInfo().get("id");
			filterClasses.put(name, info.b);
		}
		PL2.logger.info("Loaded: " + filterClasses.size() + " Filters");
	}

}
