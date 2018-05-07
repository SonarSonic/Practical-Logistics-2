package sonar.logistics.info.registries;

import appeng.helpers.IPriorityHost;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.misc.TileInscriber;
import com.google.common.collect.Lists;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.info.register.RegistryType;

@InfoRegistry(modid = "appliedenergistics2")
public class AppliedEnergisticsRegistry implements IInfoRegistry {

	public void registerBaseMethods(IMasterInfoRegistry registry) {
		registry.registerMethods(TileInscriber.class, RegistryType.TILE, Lists.newArrayList("getMaxProcessingTime", "getProcessingTime"));
		registry.registerMethods(IPriorityHost.class, RegistryType.TILE, Lists.newArrayList("getPriority"));
		
	}

	public void registerAllFields(IMasterInfoRegistry registry) {
		registry.registerFields(TileGrinder.class, RegistryType.TILE, Lists.newArrayList("points"));		
	}

	public void registerAdjustments(IMasterInfoRegistry registry) {
		registry.registerClientNames(ClientNameConstants.BASE_PROCESS_TIME, Lists.newArrayList("TileInscriber.getMaxProcessingTime"));
		registry.registerClientNames(ClientNameConstants.PROCESS_TIME, Lists.newArrayList("TileInscriber.getProcessingTime"));
		registry.registerClientNames(ClientNameConstants.PRIORITY, Lists.newArrayList("IPriorityHost.getPriority"));
		registry.registerInfoAdjustments(Lists.newArrayList("TileInscriber.getMaxProcessingTime", "TileInscriber.getProcessingTime"), "", ClientNameConstants.TICKS);
		registry.registerInfoAdjustments(Lists.newArrayList("TileGrinder.requiredTurns", "TileGrinder.points"), "", "turns");		
		
	}
}