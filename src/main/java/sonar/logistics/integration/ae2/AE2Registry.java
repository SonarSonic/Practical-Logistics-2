package sonar.logistics.integration.ae2;
/*
import appeng.helpers.IPriorityHost;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.misc.TileInscriber;
import com.google.common.collect.Lists;
import sonar.logistics.api.asm.ASMInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.ClientNameConstants;
import sonar.logistics.api.core.tiles.displays.info.register.IInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.IMasterInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;

@ASMInfoRegistry(modid = "appliedenergistics2")
public class AE2Registry implements IInfoRegistry {

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
*/