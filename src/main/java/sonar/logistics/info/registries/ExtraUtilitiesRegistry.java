package sonar.logistics.info.registries;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.machine.TileMachine;
import com.rwtema.extrautils2.tile.TileResonator;

import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.register.RegistryType;

@InfoRegistry(modid = "extrautils2")
public class ExtraUtilitiesRegistry extends IInfoRegistry {

	public void registerBaseReturns(IMasterInfoRegistry registry) {}

	public void registerBaseMethods(IMasterInfoRegistry registry) {}

	public void registerAllFields(IMasterInfoRegistry registry) {
		registry.registerFields(TileMachine.class, RegistryType.TILE, Lists.newArrayList("totalTime", "processTime"));
		registry.registerFields(TileResonator.class, RegistryType.TILE, Lists.newArrayList("progress", "processTime"));
	}

	public void registerAdjustments(IMasterInfoRegistry registry) {
		registry.registerClientNames(ClientNameConstants.BASE_PROCESS_TIME, Lists.newArrayList("TileMachine.totalTime"));
		registry.registerClientNames(ClientNameConstants.PROCESS_TIME, Lists.newArrayList("TileMachine.processTime"));
		registry.registerInfoAdjustments(Lists.newArrayList("TileMachine.totalTime", "TileMachine.processTime"), "", ClientNameConstants.TICKS);
	}
}
