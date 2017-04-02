package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.misc.TileInscriber;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.register.RegistryType;

@InfoRegistry(modid = "appliedenergistics2")
public class AppliedEnergisticsRegistry extends IInfoRegistry {

	public void registerBaseReturns(ILogicInfoRegistry registry) {
		
	}

	public void registerBaseMethods(ILogicInfoRegistry registry) {
		registry.registerMethods(TileInscriber.class, RegistryType.TILE, Lists.newArrayList("getMaxProcessingTime", "getProcessingTime"));
		registry.registerMethods(IPriorityHost.class, RegistryType.TILE, Lists.newArrayList("getPriority"));
		
	}

	public void registerAllFields(ILogicInfoRegistry registry) {
		registry.registerFields(TileGrinder.class, RegistryType.TILE, Lists.newArrayList("points"));		
	}

	public void registerAdjustments(ILogicInfoRegistry registry) {
		registry.registerClientNames(ClientNameConstants.BASE_PROCESS_TIME, Lists.newArrayList("TileInscriber.getMaxProcessingTime"));
		registry.registerClientNames(ClientNameConstants.PROCESS_TIME, Lists.newArrayList("TileInscriber.getProcessingTime"));
		registry.registerClientNames(ClientNameConstants.PRIORITY, Lists.newArrayList("IPriorityHost.getPriority"));
		registry.registerInfoAdjustments(Lists.newArrayList("TileInscriber.getMaxProcessingTime", "TileInscriber.getProcessingTime"), "", ClientNameConstants.TICKS);
		registry.registerInfoAdjustments(Lists.newArrayList("TileGrinder.requiredTurns", "TileGrinder.points"), "", "turns");		
		
	}
}
