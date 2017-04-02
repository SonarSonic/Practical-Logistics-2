package sonar.logistics.info.registries;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.machine.TileMachine;
import com.rwtema.extrautils2.power.IPower;
import com.rwtema.extrautils2.tile.TileResonator;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.register.RegistryType;

@InfoRegistry(modid = "extrautils2")
public class ExtraUtilitiesRegistry extends IInfoRegistry {

	public void registerBaseReturns(ILogicInfoRegistry registry) {
		//registry.registerReturn(TileResonator.ResonatorRecipe.class);
	}

	public void registerBaseMethods(ILogicInfoRegistry registry) {
		
	}

	public void registerAllFields(ILogicInfoRegistry registry) {
		registry.registerFields(TileMachine.class, RegistryType.TILE, Lists.newArrayList("totalTime", "processTime"));
		registry.registerFields(TileResonator.class, RegistryType.TILE, Lists.newArrayList("progress", "processTime"));
		//registry.registerFields(TileResonator.ResonatorRecipe.class, RegistryType.TILE, Lists.newArrayList("energy"));
		//registry.registerFields(IPower.class, RegistryType.TILE, Lists.newArrayList("energy"));
	}

	public void registerAdjustments(ILogicInfoRegistry registry) {
		registry.registerClientNames(ClientNameConstants.BASE_PROCESS_TIME, Lists.newArrayList("TileMachine.totalTime"));
		registry.registerClientNames(ClientNameConstants.PROCESS_TIME, Lists.newArrayList("TileMachine.processTime"));
		registry.registerInfoAdjustments(Lists.newArrayList("TileMachine.totalTime", "TileMachine.processTime"), "", ClientNameConstants.TICKS);
	}
}
